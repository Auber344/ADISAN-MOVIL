#!/usr/bin/env node
/**
 * Carga datos iniciales en el proyecto Firebase de ADISAN.
 *
 * Uso (desde la raíz del repo, con sesión iniciada en el Firebase CLI):
 *   firebase login
 *   node firebase/seed/seed.js            # se detiene si la base ya tiene datos
 *   node firebase/seed/seed.js --force    # vuelve a escribir los datos de ejemplo
 *   node firebase/seed/seed.js --emulador # carga el Firebase Local Emulator Suite (firebase emulators:start)
 *
 * Usa el token del Firebase CLI, así que escribe como dueño del proyecto (sin pasar por las reglas).
 * Las contraseñas de los usuarios de prueba se generan al azar y se guardan en
 * firebase/seed/credenciales-prueba.txt o credenciales-emulador.txt (ignorados por git).
 */
const fs = require('fs');
const os = require('os');
const path = require('path');
const crypto = require('crypto');
const { execSync } = require('child_process');

const PROYECTO = JSON.parse(fs.readFileSync(path.join(__dirname, '..', '..', '.firebaserc'), 'utf8')).projects.default;
const DOCS = `projects/${PROYECTO}/databases/(default)/documents`;
const EMULADOR = process.argv.includes('--emulador');
const FORZAR = process.argv.includes('--force') || EMULADOR;
const FIRESTORE = EMULADOR ? 'http://127.0.0.1:8080/v1' : 'https://firestore.googleapis.com/v1';
const AUTH = EMULADOR
  ? `http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/projects/${PROYECTO}`
  : `https://identitytoolkit.googleapis.com/v1/projects/${PROYECTO}`;

function token() {
  // El emulador acepta "owner" como credencial de administrador
  if (EMULADOR) return 'owner';
  const leer = () => JSON.parse(fs.readFileSync(path.join(os.homedir(), '.config', 'configstore', 'firebase-tools.json'), 'utf8')).tokens || {};
  let t = leer();
  if (!t.access_token || !t.expires_at || t.expires_at - Date.now() < 5 * 60 * 1000) {
    // Cualquier comando del CLI renueva el token guardado
    execSync('firebase projects:list', { stdio: 'ignore' });
    t = leer();
  }
  if (!t.access_token) throw new Error('No hay sesión en el Firebase CLI. Ejecuta: firebase login');
  return t.access_token;
}

async function api(method, url, body) {
  const r = await fetch(url, {
    method,
    headers: { Authorization: `Bearer ${token()}`, 'Content-Type': 'application/json', 'x-goog-user-project': PROYECTO },
    body: body ? JSON.stringify(body) : undefined,
  });
  const texto = await r.text();
  const json = texto ? JSON.parse(texto) : {};
  if (!r.ok) {
    const err = new Error(`${method} ${url} -> ${r.status} ${json.error ? json.error.message : texto}`);
    err.codigo = json.error ? json.error.message : '';
    throw err;
  }
  return json;
}

// ---------- Conversión a valores de Firestore REST ----------
const ENTERO = Symbol('entero');
const int = (n) => ({ [ENTERO]: n });

function valor(v) {
  if (v === null || v === undefined) return { nullValue: null };
  if (v instanceof Date) return { timestampValue: v.toISOString() };
  if (typeof v === 'object' && ENTERO in v) return { integerValue: String(v[ENTERO]) };
  if (typeof v === 'number') return { doubleValue: v };
  if (typeof v === 'boolean') return { booleanValue: v };
  if (typeof v === 'string') return { stringValue: v };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(valor) } };
  return { mapValue: { fields: campos(v) } };
}

function campos(obj) {
  const r = {};
  for (const [k, v] of Object.entries(obj)) r[k] = valor(v);
  return r;
}

const escritura = (ruta, datos) => ({ update: { name: `${DOCS}/${ruta}`, fields: campos(datos) } });

// ---------- Firebase Auth ----------
async function cuentaAuth(correo, password, nombre) {
  const base = AUTH;
  try {
    const r = await api('POST', `${base}/accounts`, { email: correo, password, displayName: nombre });
    return { uid: r.localId, nueva: true };
  } catch (e) {
    if (e.codigo !== 'EMAIL_EXISTS') throw e;
    const r = await api('POST', `${base}/accounts:lookup`, { email: [correo] });
    const uid = r.users[0].localId;
    await api('POST', `${base}/accounts:update`, { localId: uid, password, displayName: nombre });
    return { uid, nueva: false };
  }
}

const clave = () => crypto.randomBytes(9).toString('base64').replace(/[^a-zA-Z0-9]/g, '').slice(0, 10) + '#7';

// ---------- Datos de ejemplo ----------
const ahora = new Date();

const usuarios = [
  { nombres: 'Administrador', apellidos: 'ADISAN', dni: '70000001', correo: 'admin@adisan.pe', usuario: 'admin', rol: 'admin', telefono: '900000001', direccion: 'Oficina principal' },
  { nombres: 'Vendedor', apellidos: 'Demo', dni: '70000002', correo: 'vendedor@adisan.pe', usuario: 'vendedor', rol: 'vendedor', telefono: '900000002', direccion: 'Tienda principal' },
  { nombres: 'Chofer', apellidos: 'Demo', dni: '70000003', correo: 'chofer@adisan.pe', usuario: 'chofer', rol: 'chofer', telefono: '900000003', direccion: '' },
  { nombres: 'Cliente', apellidos: 'Demo', dni: '70000004', correo: 'cliente@adisan.pe', usuario: 'cliente', rol: 'cliente', telefono: '900000004', direccion: 'Av. Principal 123' },
];

// Cliente de mostrador sin cuenta de acceso (solo para asociarle pedidos)
const clientesSinAcceso = [
  { nombres: 'Bodega', apellidos: 'La Esquina', dni: '70000005', correo: '', usuario: '', rol: 'cliente', telefono: '900000005', direccion: 'Jr. Comercio 456' },
];

const categorias = [
  { id: 1, nombre: 'Bebidas', factor: 12 },
  { id: 2, nombre: 'Abarrotes', factor: 10 },
  { id: 3, nombre: 'Limpieza', factor: 12 },
  { id: 4, nombre: 'Snacks y galletas', factor: 6 },
];

const proveedores = [
  { id: 1, nombre_empresa: 'Distribuidora Andina (demo)', ruc: '20000000001', telefono: '014000001', correo: 'ventas@andina.example', direccion: 'Lima' },
  { id: 2, nombre_empresa: 'Abarrotes del Sur (demo)', ruc: '20000000002', telefono: '014000002', correo: 'pedidos@delsur.example', direccion: 'Arequipa' },
  { id: 3, nombre_empresa: 'Limpieza Total (demo)', ruc: '20000000003', telefono: '014000003', correo: 'contacto@limpiezatotal.example', direccion: 'Lima' },
];

// [nombre, categoria, proveedor, unidades por paquete, compra paquete, venta paquete, venta unidad, stock en unidades]
const productos = [
  ['Agua mineral 625 ml', 1, 1, 15, 12.0, 16.5, 1.5, 300],
  ['Gaseosa cola 500 ml', 1, 1, 12, 18.0, 24.0, 2.5, 240],
  ['Jugo de naranja 1 L', 1, 1, 6, 21.0, 27.0, 5.0, 60],
  ['Arroz extra 5 kg', 2, 2, 5, 95.0, 112.5, 24.0, 40],
  ['Aceite vegetal 1 L', 2, 2, 12, 84.0, 102.0, 9.5, 96],
  ['Azúcar rubia 1 kg', 2, 2, 10, 36.0, 44.0, 4.8, 8],
  ['Fideos spaghetti 500 g', 2, 2, 20, 50.0, 64.0, 3.5, 200],
  ['Detergente en polvo 500 g', 3, 3, 12, 54.0, 69.0, 6.2, 72],
  ['Lejía 1 L', 3, 3, 12, 30.0, 39.0, 3.5, 5],
  ['Galletas de vainilla x6', 4, 1, 12, 14.4, 19.2, 1.8, 144],
].map(([nombre, categoria, proveedor, factor, compra, venta, unidad, stock], i) => ({
  id: int(i + 1), nombre, categoria_id: int(categoria), proveedor_id: int(proveedor),
  unidades_por_paquete: int(factor), precio_compra_paquete: compra, precio_venta_paquete: venta,
  precio_venta_unidad: unidad, stock_unidades: int(stock), activo: int(1), created_at: ahora,
}));

const ofertas = [
  {
    id: int(1), nombre: 'Combo despensa', activo: int(1), created_at: ahora,
    items: [
      { id: int(1), producto_id: int(4), producto_nombre: 'Arroz extra 5 kg', tipo_venta: 'UNIDAD', cantidad_paquetes: int(0), cantidad_unidades: int(1) },
      { id: int(2), producto_id: int(5), producto_nombre: 'Aceite vegetal 1 L', tipo_venta: 'UNIDAD', cantidad_paquetes: int(0), cantidad_unidades: int(2) },
      { id: int(3), producto_id: int(6), producto_nombre: 'Azúcar rubia 1 kg', tipo_venta: 'UNIDAD', cantidad_paquetes: int(0), cantidad_unidades: int(2) },
    ],
  },
];
// precio normal = 24 + 2*9.5 + 2*4.8 = 52.60
ofertas[0].precio_normal = 52.6;
ofertas[0].precio_final = 47.9;
ofertas[0].descuento = Math.round((1 - 47.9 / 52.6) * 10000) / 100;

// ---------- Ejecución ----------
(async () => {
  console.log(`Proyecto: ${PROYECTO}`);
  console.log(EMULADOR ? 'Destino: Firebase Local Emulator Suite' : 'Destino: proyecto real');
  const existente = await api('GET', `${FIRESTORE}/${DOCS}/contadores/productos`).catch((e) => {
    if (String(e.message).includes(' 404 ')) return null;
    throw e;
  });
  if (existente && !FORZAR) {
    console.log('La base ya tiene datos (contadores/productos existe). Usa --force para reescribir los datos de ejemplo.');
    return;
  }

  const writes = [];
  const credenciales = [];
  let siguienteUsuario = 1;

  for (const u of usuarios) {
    const password = clave();
    const { uid, nueva } = await cuentaAuth(u.correo, password, `${u.nombres} ${u.apellidos}`);
    credenciales.push(`${u.rol.padEnd(9)} ${u.correo.padEnd(22)} ${password}`);
    writes.push(escritura(`usuarios/${uid}`, { ...u, id: int(siguienteUsuario++), ruc: '', uid, activo: int(1), created_at: ahora }));
    console.log(`  cuenta ${nueva ? 'creada' : 'actualizada'}: ${u.correo} (${u.rol})`);
  }
  for (const c of clientesSinAcceso) {
    const docId = crypto.randomBytes(10).toString('hex');
    writes.push(escritura(`usuarios/${docId}`, { ...c, id: int(siguienteUsuario++), ruc: '', uid: null, activo: int(1), created_at: ahora }));
  }

  for (const c of categorias) writes.push(escritura(`categorias/${c.id}`, { ...c, id: int(c.id), factor: int(c.factor), created_at: ahora }));
  for (const p of proveedores) writes.push(escritura(`proveedores/${p.id}`, { ...p, id: int(p.id), created_at: ahora }));
  for (const p of productos) writes.push(escritura(`productos/${p.id[ENTERO]}`, p));
  for (const o of ofertas) writes.push(escritura(`ofertas/${o.id[ENTERO]}`, o));

  const contadores = {
    usuarios: siguienteUsuario - 1, categorias: categorias.length, proveedores: proveedores.length,
    productos: productos.length, ofertas: ofertas.length,
  };
  for (const [coleccion, ultimo] of Object.entries(contadores)) writes.push(escritura(`contadores/${coleccion}`, { ultimo: int(ultimo) }));

  await api('POST', `${FIRESTORE}/${DOCS}:commit`, { writes });
  console.log(`  ${writes.length} documentos escritos en Firestore`);

  const archivo = path.join(__dirname, EMULADOR ? 'credenciales-emulador.txt' : 'credenciales-prueba.txt');
  fs.writeFileSync(archivo, [
    'Usuarios de prueba de ADISAN (generados por firebase/seed/seed.js).',
    'No subas este archivo al repositorio. Cambia estas contraseñas antes de usar la app en producción.',
    '',
    'rol       correo                 contraseña',
    ...credenciales,
    '',
  ].join(os.EOL));
  console.log(`Credenciales guardadas en ${path.relative(process.cwd(), archivo)}`);
})().catch((e) => {
  console.error('Error:', e.message);
  if (String(e.message).includes('CONFIGURATION_NOT_FOUND')) {
    console.error('Activa Authentication > Correo electrónico/contraseña en la consola de Firebase y vuelve a ejecutar.');
  }
  process.exit(1);
});
