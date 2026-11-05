const API = '/api/semilleros';
const MAX = 10;
const $ = id => document.getElementById(id);
const sel = $('semillero'), filas = $('filas');
let cantidadAntes = 0;

async function api(url, opts) {
  const r = await fetch(url, opts);
  const datos = await r.json().catch(() => null);
  if (!r.ok) throw { status: r.status, datos };
  return datos;
}

const esc = s => String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

// ---------- filas del formulario ----------
function agregarFila(v = {}) {
  if (filas.children.length >= MAX) return mostrar('err', 'Máximo 10 productos por petición');
  const d = document.createElement('div');
  d.className = 'fila';
  d.innerHTML = `
    <div class="titulo"><label>Título</label><input class="f-titulo" maxlength="200" value="${esc(v.titulo || '')}"></div>
    <div><label>Tipo</label><select class="f-tipo">
      ${['ponencia','articulo','software'].map(t => `<option ${v.tipo === t ? 'selected' : ''}>${t}</option>`).join('')}</select></div>
    <div><label>Año</label><input class="f-anio" type="number" value="${v.anio || new Date().getFullYear()}"></div>
    <div><label>Código autor</label><input class="f-autor" list="codigos" value="${esc(v.codigoAutor || '')}"></div>
    <button type="button" title="Quitar">×</button>`;
  d.querySelector('button').onclick = () => { d.remove(); contador(); };
  filas.appendChild(d);
  contador();
}
const contador = () => $('contador').textContent = `${filas.children.length} / ${MAX}`;

function leerFilas() {
  return [...filas.children].map(f => ({
    titulo: f.querySelector('.f-titulo').value.trim(),
    tipo: f.querySelector('.f-tipo').value,
    anio: parseInt(f.querySelector('.f-anio').value, 10),
    codigoAutor: f.querySelector('.f-autor').value.trim()
  }));
}

// Validación en el navegador (espejo de P2 y P3; el servidor valida de todas formas)
function validar() {
  const errores = [];
  const prods = leerFilas();
  if (prods.length < 1 || prods.length > MAX) errores.push('Debe enviar entre 1 y 10 productos');
  prods.forEach((p, i) => {
    const f = filas.children[i], n = `Producto ${i + 1}`;
    const marca = (cls, mal) => f.querySelector(cls).classList.toggle('bad', mal);
    marca('.f-titulo', !p.titulo || p.titulo.length > 200);
    marca('.f-anio', !(p.anio >= 2000 && p.anio <= 2100));
    marca('.f-autor', !p.codigoAutor);
    if (!p.titulo) errores.push(`${n}: el título es obligatorio`);
    if (p.titulo.length > 200) errores.push(`${n}: el título supera 200 caracteres`);
    if (!(p.anio >= 2000 && p.anio <= 2100)) errores.push(`${n}: el año debe estar entre 2000 y 2100`);
    if (!p.codigoAutor) errores.push(`${n}: falta el código del autor`);
  });
  return { errores, prods };
}

function mostrar(tipo, titulo, detalles = []) {
  $('mensaje').innerHTML = `<div class="msg ${tipo}"><strong>${esc(titulo)}</strong>` +
    (detalles.length ? `<ul>${detalles.map(d => `<li>${esc(d)}</li>`).join('')}</ul>` : '') + '</div>';
}

// ---------- carga de datos ----------
async function cargarSemilleros() {
  const lista = await api(API);
  sel.innerHTML = lista.map(s => `<option value="${s.id}">${esc(s.sigla)} · ${esc(s.nombre)}</option>`).join('');
  await cargarSemillero();
}

async function cargarSemillero() {
  const id = sel.value;
  const [ints] = await Promise.all([api(`${API}/${id}/integrantes`), cargarProductos()]);
  $('integrantes').innerHTML = ints.map(i =>
    `<span class="chip ${i.esLider ? 'lider' : ''}" title="${esc(i.nombre)} (${esc(i.programa)})" data-c="${esc(i.codigo)}">${esc(i.codigo)} · ${esc(i.nombre)}${i.esLider ? ' ★' : ''}</span>`).join('');
  $('codigos').innerHTML = ints.map(i => `<option value="${esc(i.codigo)}">${esc(i.nombre)}</option>`).join('');
  $('integrantes').dataset.primero = ints[0]?.codigo || '';
}

async function cargarProductos() {
  const lista = await api(`${API}/${sel.value}/productos`);
  cantidadAntes = lista.length;
  $('lista').innerHTML = lista.length ? lista.map(p => `<li>
      <span class="tipo ${esc(p.tipo)}">${esc(p.tipo)}</span><strong style="display:inline">${esc(p.titulo)}</strong>
      <small>${p.anio} · ${esc(p.nombreAutor)} (${esc(p.codigoAutor)})</small></li>`).join('')
    : '<li class="vacio">Aún no hay productos registrados</li>';
  conteos(lista.length);
}
function conteos(total, antes) {
  $('conteos').innerHTML = `<div class="count"><b>${total}</b><span>productos en el semillero</span></div>` +
    (antes !== undefined ? `<div class="count"><b>${antes}</b><span>antes de enviar</span></div>` : '');
}

// ---------- envío ----------
async function enviar() {
  $('mensaje').innerHTML = '';
  const { errores, prods } = validar();
  if (errores.length) return mostrar('err', 'Revisa el formulario (no se envió nada)', errores);

  const antes = cantidadAntes;
  $('enviar').disabled = true;
  try {
    const r = await api(`${API}/${sel.value}/productos`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ productos: prods })
    });
    mostrar('ok', `Se guardaron ${r.totalGuardados} productos en el semillero ${r.semilleroId}`);
    filas.innerHTML = ''; agregarFila();
    await cargarProductos(); conteos(cantidadAntes, antes);
  } catch (e) {
    const d = e.datos || {};
    mostrar('err', `Error ${e.status ?? ''}: ${d.mensaje || 'no se pudo conectar con el servidor'}`, d.detalles || []);
    await cargarProductos().catch(() => {}); conteos(cantidadAntes, antes);
    if (e.status === 400 || e.status === 409) {
      $('mensaje').firstChild.insertAdjacentHTML('beforeend',
        `<p style="margin:8px 0 0">Transacción revertida: el total sigue en ${cantidadAntes} (antes: ${antes}).</p>`);
    }
  } finally { $('enviar').disabled = false; }
}

// Escenario del enunciado: 3 productos, el tercero con un autor que no es del semillero
function escenarioPrueba() {
  const cods = [...document.querySelectorAll('#integrantes .chip')].map(c => c.dataset.c);
  filas.innerHTML = '';
  agregarFila({ titulo: 'Ponencia sobre microservicios', tipo: 'ponencia', anio: 2025, codigoAutor: cods[0] });
  agregarFila({ titulo: 'Artículo sobre arquitectura limpia', tipo: 'articulo', anio: 2025, codigoAutor: cods[1] || cods[0] });
  agregarFila({ titulo: 'Software de gestión de semilleros', tipo: 'software', anio: 2026, codigoAutor: '9999999' });
  mostrar('ok', 'Escenario cargado: el tercer autor (9999999) no pertenece al semillero. Pulsa «Registrar» y espera un 400.');
}

$('agregar').onclick = () => agregarFila();
$('demo').onclick = escenarioPrueba;
$('enviar').onclick = enviar;
sel.onchange = () => { $('mensaje').innerHTML = ''; cargarSemillero(); };
$('integrantes').onclick = e => {
  const c = e.target.dataset?.c; if (!c) return;
  const vacio = [...document.querySelectorAll('.f-autor')].find(i => !i.value);
  (vacio || document.querySelector('.fila:last-child .f-autor'))?.setAttribute('value', c);
  if (vacio) vacio.value = c;
};

agregarFila();
cargarSemilleros().catch(() => mostrar('err', 'No se pudo cargar los semilleros. ¿Está corriendo el backend y PostgreSQL?'));
