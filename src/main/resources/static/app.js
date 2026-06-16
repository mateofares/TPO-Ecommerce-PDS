/* ══════════════════════════════════════════════════════════════════════════
   E-Market — lógica frontend conectada al backend Spring Boot
   Refleja los mismos patrones del backend Java:
     • Strategy  → métodos de pago
     • State     → estados del pedido (Pendiente→Pagado→Enviado→Entregado)
     • Observer  → notificaciones toast al cambiar estado
     • Composite → categorías jerárquicas
   ══════════════════════════════════════════════════════════════════════════ */

const API = '/api';

// ── Estado global ────────────────────────────────────────────────────────────
const store = {
  productos:         [],
  categorias:        [],
  carrito:           [],   // { producto, cantidad }
  usuarioActual:     null, // { id, nombre, apellido, email, rol }
  metodoSeleccionado: null,
};

const ESTADOS = ['Pendiente', 'Pagado', 'Enviado', 'Entregado'];

const LABEL_PAGO = {
  TARJETA_CREDITO: 'Tarjeta de Crédito',
  TARJETA_DEBITO:  'Tarjeta de Débito',
  PAYPAL:          'PayPal',
  TRANSFERENCIA:   'Transferencia',
};

const EMOJIS_CAT = {
  'Hombre': '👔', 'Remeras': '👕', 'Pantalones': '👖', 'Buzos': '🧥',
  'Mujer': '👗', 'Vestidos': '👗', 'Calzado': '👠',
  'Accesorios': '👜', 'Bolsos': '👜', 'Relojes': '⌚',
  default: '🛍️',
};

function emojiCategoria(nombre) {
  return EMOJIS_CAT[nombre] || EMOJIS_CAT.default;
}

// ── API helpers ───────────────────────────────────────────────────────────────
async function apiGet(path) {
  const res = await fetch(API + path);
  if (!res.ok) throw new Error((await res.json()).error || res.statusText);
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(API + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || res.statusText);
  return data;
}

async function apiPut(path, body) {
  const res = await fetch(API + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || res.statusText);
  return data;
}

async function apiDelete(path) {
  const res = await fetch(API + path, { method: 'DELETE' });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || res.statusText);
  return data;
}

// ── Inicialización ────────────────────────────────────────────────────────────
async function init() {
  try {
    store.productos  = await apiGet('/productos');
    store.categorias = await apiGet('/productos/categorias');
    renderCategoriasMenu();
    renderCatalogo(store.productos);
  } catch (e) {
    console.error('Error al cargar datos del backend:', e);
    toast('Error conectando con el servidor', 'error');
  }
}

// ── Render catálogo ──────────────────────────────────────────────────────────
function renderCatalogo(productos) {
  const grid = document.getElementById('productoGrid');
  if (!productos.length) {
    grid.innerHTML = '<p style="color:var(--muted);grid-column:1/-1">No se encontraron productos.</p>';
    return;
  }
  grid.innerHTML = productos.map(p => `
    <div class="product-card">
      <div class="product-img">${emojiCategoria(p.categoria?.nombre || '')}</div>
      <div class="product-body">
        <div class="product-name">${p.nombre}</div>
        <div class="product-cat">${p.categoria?.nombre || ''}</div>
        <div class="product-price">$${p.precio.toFixed(2)}</div>
        <div class="product-stock ${p.stock === 0 ? 'sin-stock' : ''}">
          ${p.stock > 0 ? `Stock: ${p.stock}` : 'Sin stock'}
        </div>
      </div>
      <button onclick="agregarAlCarrito(${p.id})" ${p.stock === 0 ? 'disabled' : ''}>
        ${p.stock > 0 ? '+ Agregar al carrito' : 'Sin stock'}
      </button>
    </div>`).join('');
}

function renderCategoriasMenu() {
  const raices = store.categorias.filter(c => c.padreId == null);
  const ul = document.getElementById('categoriaList');
  const sel = document.getElementById('searchCategory');

  ul.innerHTML = `<li class="active" onclick="filtrarPorCategoria(null, this)">Todos</li>`;
  raices.forEach(cat => {
    ul.innerHTML += `<li onclick="filtrarPorCategoria('${cat.nombre}', this)">${cat.nombre}</li>`;
    const hijos = store.categorias.filter(c => c.padreId === cat.id);
    hijos.forEach(h => {
      ul.innerHTML += `<li style="padding-left:1.5rem;font-size:.88rem" onclick="filtrarPorCategoria('${h.nombre}', this)">↳ ${h.nombre}</li>`;
    });
  });

  sel.innerHTML = '<option value="">Todas las categorías</option>';
  store.categorias.forEach(c => {
    sel.innerHTML += `<option value="${c.nombre}">${c.nombre}</option>`;
  });
}

function filtrarPorCategoria(categoria, el) {
  document.querySelectorAll('.sidebar li').forEach(li => li.classList.remove('active'));
  el.classList.add('active');
  const titulo = document.getElementById('catalogoTitulo');
  if (!categoria) {
    titulo.textContent = 'Todos los productos';
    renderCatalogo(store.productos);
  } else {
    titulo.textContent = categoria;
    const cat = store.categorias.find(c => c.nombre === categoria);
    const ids = new Set();
    if (cat) {
      ids.add(cat.id);
      store.categorias.filter(c => c.padreId === cat.id).forEach(c => ids.add(c.id));
    }
    renderCatalogo(store.productos.filter(p => p.categoria && ids.has(p.categoria.id)));
  }
  mostrarSeccion('catalogo');
}

// ── Búsqueda ─────────────────────────────────────────────────────────────────
function buscarProductos() {
  const texto  = document.getElementById('searchInput').value.toLowerCase().trim();
  const cat    = document.getElementById('searchCategory').value;
  const minVal = parseFloat(document.getElementById('priceMin').value);
  const maxVal = parseFloat(document.getElementById('priceMax').value);

  let resultado = store.productos;
  if (texto)          resultado = resultado.filter(p => p.nombre.toLowerCase().includes(texto));
  if (cat)            resultado = resultado.filter(p => p.categoria?.nombre === cat);
  if (!isNaN(minVal)) resultado = resultado.filter(p => p.precio >= minVal);
  if (!isNaN(maxVal)) resultado = resultado.filter(p => p.precio <= maxVal);

  document.getElementById('catalogoTitulo').textContent =
    texto || cat ? 'Resultados de búsqueda' : 'Todos los productos';
  mostrarSeccion('catalogo');
  renderCatalogo(resultado);
}

document.getElementById('searchInput').addEventListener('keydown', e => {
  if (e.key === 'Enter') buscarProductos();
});

// ── Carrito (local, se envía al confirmar) ────────────────────────────────────
function agregarAlCarrito(productoId) {
  if (!store.usuarioActual) {
    toast('Iniciá sesión para agregar productos', 'error');
    abrirModal('modalLogin');
    return;
  }
  if (store.usuarioActual.rol === 'ADMINISTRADOR') {
    toast('Los administradores no pueden comprar', 'error');
    return;
  }

  const prod = store.productos.find(p => p.id === productoId);
  const item = store.carrito.find(i => i.producto.id === productoId);
  const cantEnCarrito = item ? item.cantidad : 0;

  if (cantEnCarrito + 1 > prod.stock) {
    toast(`Stock insuficiente para ${prod.nombre}`, 'error');
    return;
  }

  if (item) { item.cantidad++; }
  else { store.carrito.push({ producto: prod, cantidad: 1 }); }

  actualizarBadgeCarrito();
  renderCarrito();
  toast(`${prod.nombre} agregado al carrito ✓`, 'success');
}

function cambiarCantidad(productoId, delta) {
  const item = store.carrito.find(i => i.producto.id === productoId);
  if (!item) return;
  const nueva = item.cantidad + delta;
  if (nueva <= 0) {
    store.carrito = store.carrito.filter(i => i.producto.id !== productoId);
  } else if (nueva > item.producto.stock) {
    toast('Cantidad máxima alcanzada', 'error');
  } else {
    item.cantidad = nueva;
  }
  actualizarBadgeCarrito();
  renderCarrito();
}

function eliminarDelCarrito(productoId) {
  store.carrito = store.carrito.filter(i => i.producto.id !== productoId);
  actualizarBadgeCarrito();
  renderCarrito();
}

function calcularTotalCarrito() {
  return store.carrito.reduce((sum, i) => sum + i.producto.precio * i.cantidad, 0);
}

function actualizarBadgeCarrito() {
  const total = store.carrito.reduce((s, i) => s + i.cantidad, 0);
  const badge = document.getElementById('cartBadge');
  badge.textContent = total;
  badge.classList.toggle('hidden', total === 0);
}

function renderCarrito() {
  const container = document.getElementById('carritoItems');
  const totalEl   = document.getElementById('carritoTotal');

  if (store.carrito.length === 0) {
    container.innerHTML = `<div class="carrito-vacio"><div>🛒</div>El carrito está vacío</div>`;
    totalEl.textContent = '$0.00';
    return;
  }

  container.innerHTML = store.carrito.map(i => `
    <div class="cart-item">
      <div class="cart-item-icon">${emojiCategoria(i.producto.categoria?.nombre || '')}</div>
      <div class="cart-item-info">
        <div class="cart-item-name">${i.producto.nombre}</div>
        <div class="cart-item-price">$${(i.producto.precio * i.cantidad).toFixed(2)}</div>
      </div>
      <div class="cart-item-controls">
        <button onclick="cambiarCantidad(${i.producto.id}, -1)">−</button>
        <span>${i.cantidad}</span>
        <button onclick="cambiarCantidad(${i.producto.id}, +1)">+</button>
        <button class="cart-item-delete" onclick="eliminarDelCarrito(${i.producto.id})">🗑</button>
      </div>
    </div>`).join('');

  totalEl.textContent = `$${calcularTotalCarrito().toFixed(2)}`;
}

function toggleCarrito() {
  const panel   = document.getElementById('carritoPanel');
  const overlay = document.getElementById('overlay');
  const isOpen  = panel.classList.contains('open');
  if (isOpen) {
    panel.classList.remove('open');
    overlay.classList.add('hidden');
    setTimeout(() => panel.classList.add('hidden'), 300);
  } else {
    panel.classList.remove('hidden');
    renderCarrito();
    panel.classList.add('open');
    overlay.classList.remove('hidden');
  }
}

function cerrarTodo() {
  const panel = document.getElementById('carritoPanel');
  panel.classList.remove('open');
  document.getElementById('overlay').classList.add('hidden');
  setTimeout(() => panel.classList.add('hidden'), 300);
}

// ── Checkout ─────────────────────────────────────────────────────────────────
function irACheckout() {
  if (store.carrito.length === 0) return;
  cerrarTodo();

  const resumen = document.getElementById('checkoutResumen');
  const items = store.carrito.map(i =>
    `<div class="item-line"><span>${i.producto.nombre} x${i.cantidad}</span><span>$${(i.producto.precio * i.cantidad).toFixed(2)}</span></div>`
  ).join('');
  resumen.innerHTML = items + `<div class="total-line"><span>Total</span><span>$${calcularTotalCarrito().toFixed(2)}</span></div>`;

  store.metodoSeleccionado = null;
  document.querySelectorAll('.payment-card').forEach(c => c.classList.remove('selected'));
  document.querySelectorAll('.pago-form').forEach(f => f.classList.add('hidden'));
  document.querySelectorAll('input[name="pago"]').forEach(r => r.checked = false);
  ocultarError('checkoutError');

  abrirModal('modalCheckout');
}

function seleccionarPago(metodo) {
  store.metodoSeleccionado = metodo;
  document.querySelectorAll('.payment-card').forEach(c => c.classList.remove('selected'));
  document.querySelector(`input[value="${metodo}"]`)?.closest('.payment-card')?.classList.add('selected');

  document.getElementById('pagoFormTarjeta').classList.toggle('hidden', metodo !== 'tarjeta');
  document.getElementById('pagoFormPaypal').classList.toggle('hidden', metodo !== 'paypal');
  document.getElementById('pagoFormTransferencia').classList.toggle('hidden', metodo !== 'transferencia');
}

async function confirmarCompra() {
  ocultarError('checkoutError');

  if (!store.metodoSeleccionado) {
    mostrarError('checkoutError', 'Seleccioná un método de pago.');
    return;
  }

  // Construir request según el Patrón Strategy (Strategy pattern)
  const body = {
    clienteId: store.usuarioActual.id,
    items: store.carrito.map(i => ({ productoId: i.producto.id, cantidad: i.cantidad })),
  };

  if (store.metodoSeleccionado === 'tarjeta') {
    const num = document.getElementById('cardNumero').value.trim();
    const cvv = document.getElementById('cardCVV').value.trim();
    if (num.length < 16 || !/^\d+$/.test(num)) { mostrarError('checkoutError', 'Número de tarjeta inválido.'); return; }
    if (cvv.length < 3)  { mostrarError('checkoutError', 'CVV inválido.'); return; }
    body.tipoPago = 'TARJETA_CREDITO';
    body.numero = num;
    body.cvv = cvv;
  } else if (store.metodoSeleccionado === 'paypal') {
    const cuenta = document.getElementById('ppCuenta').value.trim();
    const token  = document.getElementById('ppToken').value.trim();
    if (!cuenta.includes('@')) { mostrarError('checkoutError', 'Email de PayPal inválido.'); return; }
    body.tipoPago = 'PAYPAL';
    body.cuenta = cuenta;
    body.token = token || 'tok_demo';
  } else if (store.metodoSeleccionado === 'transferencia') {
    const cbu   = document.getElementById('transCBU').value.trim();
    const banco = document.getElementById('transBanco').value.trim();
    if (cbu.length < 22) { mostrarError('checkoutError', 'CBU inválido (22 dígitos).'); return; }
    if (!banco) { mostrarError('checkoutError', 'Ingresá el nombre del banco.'); return; }
    body.tipoPago = 'TRANSFERENCIA';
    body.cbu = cbu;
    body.banco = banco;
  }

  try {
    const pedido = await apiPost('/pedidos/confirmar', body);
    store.carrito = [];
    actualizarBadgeCarrito();
    cerrarModal('modalCheckout');

    // Recargar productos para reflejar stock actualizado
    store.productos = await apiGet('/productos');
    renderCatalogo(store.productos);

    // Observer pattern: suscribir notificación SMS (demo)
    await apiPost(`/pedidos/${pedido.id}/suscribir`, { tipo: 'SMS', destino: '+5491100000000' });

    toast(`¡Pedido #${pedido.id} confirmado! Total: $${pedido.total.toFixed(2)} ✓`, 'success');
  } catch (e) {
    mostrarError('checkoutError', e.message);
  }
}

// ── Pedidos ──────────────────────────────────────────────────────────────────
async function renderMisPedidos() {
  const el = document.getElementById('misPedidosList');
  try {
    const pedidos = await apiGet(`/pedidos/cliente/${store.usuarioActual.id}`);
    if (!pedidos.length) {
      el.innerHTML = '<p style="color:var(--muted)">No tenés pedidos todavía.</p>';
      return;
    }
    el.innerHTML = pedidos.map(p => renderPedidoCard(p, false)).join('');
  } catch (e) {
    el.innerHTML = `<p style="color:var(--danger)">${e.message}</p>`;
  }
}

async function renderAdminPedidos() {
  const el = document.getElementById('todosLosPedidosList');
  try {
    const pedidos = await apiGet('/pedidos');
    if (!pedidos.length) {
      el.innerHTML = '<p style="color:var(--muted)">No hay pedidos registrados.</p>';
      return;
    }
    el.innerHTML = pedidos.map(p => renderPedidoCard(p, true)).join('');
  } catch (e) {
    el.innerHTML = `<p style="color:var(--danger)">${e.message}</p>`;
  }
}

function renderPedidoCard(pedido, esAdmin) {
  const estadoNombre = pedido.estadoNombreStr || pedido.estadoNombre || '—';
  const sigEstado = siguienteEstado(estadoNombre);
  const itemsTexto = (pedido.items || [])
    .map(i => `${i.producto?.nombre || '—'} x${i.cantidad} ($${(i.precioUnitario * i.cantidad).toFixed(2)})`)
    .join(', ') || '—';
  const metodoPago = LABEL_PAGO[pedido.tipoPago] || pedido.tipoPago || '';
  const total = typeof pedido.total === 'number' ? pedido.total.toFixed(2) : '—';

  const btnAvanzar = esAdmin && sigEstado
    ? `<button onclick="avanzarEstadoPedido(${pedido.id})">→ ${sigEstado}</button>`
    : '';

  return `
    <div class="pedido-card" id="pedido-${pedido.id}">
      <div class="pedido-header">
        <div><span class="pedido-id">Pedido #${pedido.id}</span></div>
        <span class="estado-badge estado-${estadoNombre}">${estadoNombre}</span>
      </div>
      <div class="pedido-items">${itemsTexto}</div>
      <div class="pedido-footer">
        <span class="pedido-fecha">${pedido.fecha || ''} · ${metodoPago}</span>
        <span class="pedido-total">$${total}</span>
        ${esAdmin ? `<div class="admin-actions">${btnAvanzar}</div>` : ''}
      </div>
    </div>`;
}

function siguienteEstado(estadoActual) {
  const idx = ESTADOS.indexOf(estadoActual);
  return idx >= 0 && idx < ESTADOS.length - 1 ? ESTADOS[idx + 1] : null;
}

async function avanzarEstadoPedido(pedidoId) {
  try {
    // State pattern: el backend aplica avanzarEstado() → notifica observadores (Observer)
    await apiPut(`/pedidos/${pedidoId}/avanzar`, {});
    toast(`Pedido #${pedidoId} avanzó de estado`, 'success');
    renderAdminPedidos();
  } catch (e) {
    toast(e.message, 'error');
  }
}

// ── Auth ─────────────────────────────────────────────────────────────────────
async function registrar() {
  ocultarError('regError');
  const nombre   = document.getElementById('regNombre').value.trim();
  const apellido = document.getElementById('regApellido').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const pass     = document.getElementById('regPass').value;
  const rol      = document.getElementById('regRol').value;
  const legajo   = parseInt(document.getElementById('regLegajo').value) || 0;

  if (!nombre || !apellido || !email || !pass) { mostrarError('regError', 'Completá todos los campos.'); return; }
  if (pass.length < 4) { mostrarError('regError', 'La contraseña debe tener al menos 4 caracteres.'); return; }
  if (rol === 'admin' && !legajo) { mostrarError('regError', 'El legajo es obligatorio para administradores.'); return; }

  try {
    let usuario;
    if (rol === 'cliente') {
      usuario = await apiPost('/usuarios/registro/cliente', { nombre, apellido, email, contrasenia: pass });
    } else {
      usuario = await apiPost('/usuarios/registro/admin', { nombre, apellido, email, contrasenia: pass, legajo });
    }
    cerrarModal('modalRegistro');
    iniciarSesion({ ...usuario, rol: usuario.rol || (rol === 'admin' ? 'ADMINISTRADOR' : 'CLIENTE') });
    toast(`Bienvenido/a, ${nombre}! Cuenta creada ✓`, 'success');
  } catch (e) {
    mostrarError('regError', e.message);
  }
}

async function login() {
  ocultarError('loginError');
  const email = document.getElementById('loginEmail').value.trim();
  const pass  = document.getElementById('loginPass').value;

  try {
    const usuario = await apiPost('/usuarios/login', { email, contrasenia: pass });
    cerrarModal('modalLogin');
    iniciarSesion(usuario);
    toast(`Bienvenido/a, ${usuario.nombre}!`, 'success');
  } catch (e) {
    mostrarError('loginError', e.message);
  }
}

function iniciarSesion(usuario) {
  store.usuarioActual = usuario;
  document.getElementById('userArea').classList.add('hidden');
  document.getElementById('userMenu').classList.remove('hidden');
  document.getElementById('userNameDisplay').textContent =
    `${usuario.nombre} (${usuario.rol === 'ADMINISTRADOR' ? 'Admin' : 'Cliente'})`;
  document.getElementById('btnAdmin').classList.toggle('hidden', usuario.rol !== 'ADMINISTRADOR');
}

function cerrarSesion() {
  store.usuarioActual = null;
  store.carrito = [];
  actualizarBadgeCarrito();
  document.getElementById('userArea').classList.remove('hidden');
  document.getElementById('userMenu').classList.add('hidden');
  mostrarSeccion('catalogo');
  toast('Sesión cerrada.');
}

document.getElementById('regRol').addEventListener('change', function () {
  document.getElementById('legajoField').classList.toggle('hidden', this.value !== 'admin');
});

// ── Navegación ───────────────────────────────────────────────────────────────
function mostrarSeccion(id) {
  document.getElementById('seccionCatalogo').classList.toggle('hidden', id !== 'catalogo');
  document.getElementById('seccionPedidos').classList.toggle('hidden', id !== 'pedidos');
  document.getElementById('seccionAdmin').classList.toggle('hidden', id !== 'admin');

  if (id === 'pedidos') renderMisPedidos();
  if (id === 'admin')   renderAdminPedidos();
}

// ── Modales ──────────────────────────────────────────────────────────────────
function abrirModal(id) {
  document.getElementById(id).classList.remove('hidden');
  document.getElementById('overlay').classList.remove('hidden');
}
function cerrarModal(id) {
  document.getElementById(id).classList.add('hidden');
  if (!document.getElementById('carritoPanel').classList.contains('open')) {
    document.getElementById('overlay').classList.add('hidden');
  }
}
function switchModal(cerrar, abrir) { cerrarModal(cerrar); abrirModal(abrir); }

document.addEventListener('keydown', e => {
  if (e.key === 'Escape') {
    ['modalLogin','modalRegistro','modalCheckout'].forEach(id => {
      document.getElementById(id)?.classList.add('hidden');
    });
    cerrarTodo();
  }
});

// ── Utilidades ───────────────────────────────────────────────────────────────
function mostrarError(id, msg) {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.classList.remove('hidden');
}
function ocultarError(id) {
  document.getElementById(id)?.classList.add('hidden');
}

let toastTimer;
function toast(msg, tipo = '') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `toast ${tipo}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.add('hidden'), 3500);
}

// ── Init ─────────────────────────────────────────────────────────────────────
init();
