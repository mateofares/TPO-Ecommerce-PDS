/* ══════════════════════════════════════════════════════════════════════════
   E-Market — lógica frontend (conectada a API Java)
   Refleja los mismos patrones del backend Java:
     • Strategy  → métodos de pago
     • State     → estados del pedido (Pendiente→Pagado→Enviado→Entregado)
     • Observer  → notificaciones toast al cambiar estado
     • Composite → categorías jerárquicas
   ══════════════════════════════════════════════════════════════════════════ */

// ── Configuración de API ─────────────────────────────────────────────────────
const API_BASE_URL = 'http://localhost:8080/api';

// ── Estado global ────────────────────────────────────────────────────────────
const store = {
  usuarios:       [],   // { id, nombre, apellido, email, pass, rol, legajo? }
  productos:      [],   // { id, nombre, precio, stock, categoria, emoji }
  categorias:     [],   // { id, nombre, padreId? }
  pedidos:        [],   // { id, clienteId, fecha, estado, metodoPago, items[], total }
  carrito:        [],   // { producto, cantidad }
  usuarioActual:  null,
  metodoSeleccionado: null,
};

const ESTADOS_CSS = ['Pendiente', 'Pagado', 'Enviado', 'Entregado']; // solo para clases CSS

const EMOJIS_CAT = {
  'Electrónica': '💻', 'Celulares': '📱', 'Laptops': '💻',
  'Ropa': '👕', 'Calzado': '👟', 'Deportes': '⚽',
  'Hogar': '🏠', 'Cocina': '🍳',
  default: '📦',
};

// ── Inicialización ──────────────────────────────────────────────────────────
async function inicializarDatos() {
  try {
    // Cargar productos del API
    const respProductos = await fetch(`${API_BASE_URL}/productos`);
    store.productos = await respProductos.json();
    
    // Extraer categorías únicas
    const catsSet = new Set();
    store.productos.forEach(p => {
      if (p.categoria && p.categoria.nombre) {
        catsSet.add(JSON.stringify(p.categoria));
      }
    });
    store.categorias = Array.from(catsSet).map(c => JSON.parse(c));
    
    console.log('[API] Datos cargados:', store.productos.length, 'productos');
  } catch (error) {
    console.error('[API ERROR]', error);
    toast('Error al conectar con el servidor', 'error');
  }
}

// ── Helper para emojis ──────────────────────────────────────────────────────
function getEmojiProducto(nombre) {
  for (const [cat, emoji] of Object.entries(EMOJIS_CAT)) {
    if (cat !== 'default' && nombre.toLowerCase().includes(cat.toLowerCase())) {
      return emoji;
    }
  }
  return EMOJIS_CAT.default;
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
      <div class="product-img">${getEmojiProducto(p.nombre)}</div>
      <div class="product-body">
        <div class="product-name">${p.nombre}</div>
        <div class="product-cat">${p.categoria?.nombre || 'General'}</div>
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
  const ul = document.getElementById('categoriaList');
  const sel = document.getElementById('searchCategory');

  // sidebar
  ul.innerHTML = `<li class="active" onclick="filtrarPorCategoria(null, this)">Todos</li>`;
  const categoriasPorNombre = {};
  store.categorias.forEach(cat => {
    if (cat.nombre) {
      categoriasPorNombre[cat.nombre] = true;
      ul.innerHTML += `<li onclick="filtrarPorCategoria('${cat.nombre}', this)">${cat.nombre}</li>`;
    }
  });

  // select de búsqueda
  sel.innerHTML = '<option value="">Todas las categorías</option>';
  Object.keys(categoriasPorNombre).forEach(nombre => {
    sel.innerHTML += `<option value="${nombre}">${nombre}</option>`;
  });
}

function filtrarPorCategoria(categoria, el) {
  document.querySelectorAll('.sidebar li').forEach(li => li.classList.remove('active'));
  if (el) el.classList.add('active');

  const titulo = document.getElementById('catalogoTitulo');
  if (!categoria) {
    titulo.textContent = 'Todos los productos';
    renderCatalogo(store.productos);
  } else {
    titulo.textContent = categoria;
    renderCatalogo(store.productos.filter(p => p.categoria?.nombre === categoria));
  }
  mostrarSeccion('catalogo');
}

// ── Búsqueda ─────────────────────────────────────────────────────────────────
async function buscarProductos() {
  const texto    = document.getElementById('searchInput').value.toLowerCase().trim();
  const cat      = document.getElementById('searchCategory').value;
  const minVal   = parseFloat(document.getElementById('priceMin').value);
  const maxVal   = parseFloat(document.getElementById('priceMax').value);

  try {
    let url = `${API_BASE_URL}/productos`;
    const params = new URLSearchParams();

    if (texto) params.append('nombre', texto);
    if (cat) params.append('categoria', cat);
    if (!isNaN(minVal)) params.append('precioMin', minVal);
    if (!isNaN(maxVal)) params.append('precioMax', maxVal);

    if (params.toString()) {
      url += '?' + params.toString();
    }

    const resp = await fetch(url);
    const resultado = await resp.json();

    document.getElementById('catalogoTitulo').textContent =
      texto || cat ? `Resultados de búsqueda` : 'Todos los productos';
    mostrarSeccion('catalogo');
    renderCatalogo(resultado);

  } catch (error) {
    console.error('[ERROR] Búsqueda fallida:', error);
    toast('Error al buscar productos', 'error');
  }
}

document.getElementById('searchInput').addEventListener('keydown', e => {
  if (e.key === 'Enter') buscarProductos();
});

// ── Carrito ──────────────────────────────────────────────────────────────────
function agregarAlCarrito(productoId) {
  if (!store.usuarioActual) {
    toast('Iniciá sesión para agregar productos', 'error');
    abrirModal('modalLogin');
    return;
  }
  if (store.usuarioActual.rol === 'admin') {
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

  if (item) {
    item.cantidad++;
  } else {
    store.carrito.push({ producto: prod, cantidad: 1 });
  }

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
      <div class="cart-item-icon">${getEmojiProducto(i.producto.nombre)}</div>
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
    panel.classList.add('hidden');
    overlay.classList.add('hidden');
  } else {
    renderCarrito();
    panel.classList.remove('hidden');
    panel.classList.add('open');
    overlay.classList.remove('hidden');
  }
}

function cerrarTodo() {
  const panel = document.getElementById('carritoPanel');
  panel.classList.remove('open');
  panel.classList.add('hidden');
  document.getElementById('overlay').classList.add('hidden');
}

// ── Checkout ─────────────────────────────────────────────────────────────────
function irACheckout() {
  if (store.carrito.length === 0) return;
  cerrarTodo();

  // Resumen
  const resumen = document.getElementById('checkoutResumen');
  const items = store.carrito.map(i =>
    `<div class="item-line"><span>${i.producto.nombre} x${i.cantidad}</span><span>$${(i.producto.precio * i.cantidad).toFixed(2)}</span></div>`
  ).join('');
  resumen.innerHTML = items + `<div class="total-line"><span>Total</span><span>$${calcularTotalCarrito().toFixed(2)}</span></div>`;

  // Reset formulario pago
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

function confirmarCompra() {
  ocultarError('checkoutError');

  if (!store.metodoSeleccionado) {
    mostrarError('checkoutError', 'Seleccioná un método de pago.');
    return;
  }

  let detallesPago = {};
  if (store.metodoSeleccionado === 'tarjeta') {
    const num = document.getElementById('cardNumero').value.trim();
    const cvv = document.getElementById('cardCVV').value.trim();
    if (num.length < 16 || !/^\d+$/.test(num)) {
      mostrarError('checkoutError', 'Número de tarjeta inválido (16 dígitos).'); return;
    }
    if (cvv.length < 3 || !/^\d+$/.test(cvv)) {
      mostrarError('checkoutError', 'CVV inválido.'); return;
    }
    detallesPago = { numero: num, cvv };
  } else if (store.metodoSeleccionado === 'paypal') {
    const cuenta = document.getElementById('ppCuenta').value.trim();
    if (!cuenta.includes('@')) {
      mostrarError('checkoutError', 'Email de PayPal inválido.'); return;
    }
    detallesPago = { cuenta };
  } else if (store.metodoSeleccionado === 'transferencia') {
    const cbu   = document.getElementById('transCBU').value.trim();
    const banco = document.getElementById('transBanco').value.trim();
    if (cbu.length < 22 || !/^\d+$/.test(cbu)) {
      mostrarError('checkoutError', 'CBU inválido (22 dígitos).'); return;
    }
    if (!banco) {
      mostrarError('checkoutError', 'Ingresá el nombre del banco.'); return;
    }
    detallesPago = { cbu, banco };
  }

  for (const item of store.carrito) {
    if (item.producto.stock < item.cantidad) {
      mostrarError('checkoutError', `Stock insuficiente para ${item.producto.nombre}.`);
      return;
    }
  }

  crearPedidoEnAPI(store.metodoSeleccionado, detallesPago);
}

async function crearPedidoEnAPI(metodoPago, detallesPago) {
  try {
    const items = store.carrito.map(i => ({
      productoId: i.producto.id,
      cantidad: i.cantidad
    }));

    const resp = await fetch(`${API_BASE_URL}/pedidos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        usuarioId: store.usuarioActual.id,
        metodoPago: metodoPago,
        detallesPago: detallesPago,
        items: items
      })
    });

    const data = await resp.json();

    if (!resp.ok) {
      mostrarError('checkoutError', data.error || 'Error al confirmar compra');
      return;
    }

    // Limpiar carrito
    store.carrito = [];
    actualizarBadgeCarrito();
    cerrarModal('modalCheckout');

    toast(`¡Pedido confirmado! Pago procesado. ✓`, 'success');

    // Recargar productos desde la API para reflejar el stock actualizado
    const respProductos = await fetch(`${API_BASE_URL}/productos`);
    store.productos = await respProductos.json();
    renderCatalogo(store.productos);

  } catch (error) {
    mostrarError('checkoutError', 'Error al conectar: ' + error.message);
  }
}

// ── Pedidos ──────────────────────────────────────────────────────────────────
async function renderMisPedidos() {
  try {
    const resp = await fetch(`${API_BASE_URL}/pedidos?usuarioId=${store.usuarioActual.id}`);
    const pedidos = await resp.json();
    
    const el = document.getElementById('misPedidosList');
    if (!pedidos.length) {
      el.innerHTML = '<p style="color:var(--muted)">No tenés pedidos todavía.</p>';
      return;
    }
    el.innerHTML = pedidos.map(p => renderPedidoCard(p, false)).join('');
  } catch (error) {
    console.error('[ERROR] Cargar pedidos:', error);
    document.getElementById('misPedidosList').innerHTML = '<p style="color:red">Error al cargar pedidos</p>';
  }
}

async function renderAdminPedidos() {
  try {
    const resp = await fetch(`${API_BASE_URL}/pedidos`);
    const pedidos = await resp.json();
    
    const el = document.getElementById('todosLosPedidosList');
    if (!pedidos.length) {
      el.innerHTML = '<p style="color:var(--muted)">No hay pedidos registrados.</p>';
      return;
    }
    el.innerHTML = pedidos.map(p => renderPedidoCard(p, true)).join('');
  } catch (error) {
    console.error('[ERROR] Cargar pedidos admin:', error);
    document.getElementById('todosLosPedidosList').innerHTML = '<p style="color:red">Error al cargar pedidos</p>';
  }
}

function renderPedidoCard(pedido, esAdmin) {
  const itemsTexto = pedido.items.map(i => `${i.producto.nombre} x${i.cantidad}`).join(', ');

  const btnAvanzar = esAdmin && pedido.puedeAvanzar
    ? `<button onclick="avanzarEstadoPedido(${pedido.id})">Avanzar estado</button>`
    : '';

  return `
    <div class="pedido-card" id="pedido-${pedido.id}">
      <div class="pedido-header">
        <div>
          <span class="pedido-id">Pedido #${pedido.id}</span>
        </div>
        <span class="estado-badge estado-${pedido.estado}">${pedido.estado}</span>
      </div>
      <div class="pedido-items">${itemsTexto}</div>
      <div class="pedido-footer">
        <span class="pedido-fecha">${pedido.fecha} · ${pedido.metodoPago}</span>
        <span class="pedido-total">$${pedido.total.toFixed(2)}</span>
        ${esAdmin ? `<div class="admin-actions">${btnAvanzar}</div>` : ''}
      </div>
    </div>`;
}


async function avanzarEstadoPedido(pedidoId) {
  try {
    const resp = await fetch(`${API_BASE_URL}/pedidos/${pedidoId}/avanzar`, { method: 'PUT' });
    const data = await resp.json();
    if (!resp.ok) {
      toast(data.error || 'Error al avanzar estado', 'error');
      return;
    }
    renderAdminPedidos();
    toast(`Pedido #${pedidoId} → ${data.pedido.estado}`, 'success');
  } catch (error) {
    toast('Error: ' + error.message, 'error');
  }
}

// ── Auth ─────────────────────────────────────────────────────────────────────
async function registrar() {
  ocultarError('regError');
  const nombre   = document.getElementById('regNombre').value.trim();
  const apellido = document.getElementById('regApellido').value.trim();
  const email    = document.getElementById('regEmail').value.trim().toLowerCase();
  const pass     = document.getElementById('regPass').value;
  const rol      = document.getElementById('regRol').value;
  const legajo   = parseInt(document.getElementById('regLegajo').value) || null;

  if (!nombre || !apellido || !email || !pass) {
    mostrarError('regError', 'Completá todos los campos.'); return;
  }
  if (!email.includes('@')) {
    mostrarError('regError', 'Email inválido.'); return;
  }
  if (pass.length < 4) {
    mostrarError('regError', 'La contraseña debe tener al menos 4 caracteres.'); return;
  }
  if (rol === 'admin' && !legajo) {
    mostrarError('regError', 'El legajo es obligatorio para administradores.'); return;
  }

  try {
    const payload = { nombre, apellido, email, pass, rol };
    if (rol === 'admin') payload.legajo = legajo;

    const resp = await fetch(`${API_BASE_URL}/usuarios/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await resp.json();

    if (!resp.ok) {
      mostrarError('regError', data.error || 'Error en el registro');
      return;
    }

    cerrarModal('modalRegistro');
    iniciarSesion(data.usuario);
    toast(`Bienvenido/a, ${nombre}! Cuenta creada. ✓`, 'success');

  } catch (error) {
    mostrarError('regError', 'Error al conectar con el servidor: ' + error.message);
  }
}

async function login() {
  ocultarError('loginError');
  const email = document.getElementById('loginEmail').value.trim().toLowerCase();
  const pass  = document.getElementById('loginPass').value;

  if (!email || !pass) {
    mostrarError('loginError', 'Completá email y contraseña.');
    return;
  }

  try {
    const resp = await fetch(`${API_BASE_URL}/usuarios/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, pass })
    });

    const data = await resp.json();

    if (!resp.ok) {
      mostrarError('loginError', data.error || 'Error en login');
      return;
    }

    cerrarModal('modalLogin');
    iniciarSesion(data.usuario);
    toast(`Bienvenido/a, ${data.usuario.nombre}!`, 'success');

  } catch (error) {
    mostrarError('loginError', 'Error al conectar con el servidor: ' + error.message);
  }
}

function iniciarSesion(usuario) {
  store.usuarioActual = usuario;
  document.getElementById('userArea').classList.add('hidden');
  document.getElementById('userMenu').classList.remove('hidden');
  document.getElementById('userNameDisplay').textContent =
    `${usuario.nombre} (${usuario.rol === 'admin' ? 'Admin' : 'Cliente'})`;
  document.getElementById('btnAdmin').classList.toggle('hidden', usuario.rol !== 'admin');
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

// Mostrar campo legajo cuando se elige admin en el registro
document.getElementById('regRol').addEventListener('change', function () {
  document.getElementById('legajoField').classList.toggle('hidden', this.value !== 'admin');
});

// ── Navegación de secciones ──────────────────────────────────────────────────
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
function switchModal(cerrar, abrir) {
  cerrarModal(cerrar);
  abrirModal(abrir);
}

// Cerrar modal con Escape
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
(async function() {
  console.log('[INIT] Conectando con API...');
  await inicializarDatos();
  renderCategoriasMenu();
  renderCatalogo(store.productos);
  toast('Conectado al servidor ✓', 'success');
})();
