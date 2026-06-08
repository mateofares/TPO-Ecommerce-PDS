/* ══════════════════════════════════════════════════════════════════════════
   E-Market — lógica frontend
   Refleja los mismos patrones del backend Java:
     • Strategy  → métodos de pago
     • State     → estados del pedido (Pendiente→Pagado→Enviado→Entregado)
     • Observer  → notificaciones toast al cambiar estado
     • Composite → categorías jerárquicas
   ══════════════════════════════════════════════════════════════════════════ */

// ── Estado global ────────────────────────────────────────────────────────────
const store = {
  usuarios:       [],   // { id, nombre, apellido, email, pass, rol, legajo? }
  productos:      [],   // { id, nombre, precio, stock, categoria, emoji }
  categorias:     [],   // { id, nombre, padreId? }
  pedidos:        [],   // { id, clienteId, fecha, estado, metodoPago, items[], total }
  carrito:        [],   // { producto, cantidad }
  usuarioActual:  null,
  nextUserId:     1,
  nextPedidoId:   1,
  metodoSeleccionado: null,
};

const ESTADOS = ['Pendiente', 'Pagado', 'Enviado', 'Entregado'];

const EMOJIS_CAT = {
  'Electrónica': '💻', 'Celulares': '📱', 'Laptops': '💻',
  'Ropa': '👕', 'Calzado': '👟', 'Deportes': '⚽',
  'Hogar': '🏠', 'Cocina': '🍳',
  default: '📦',
};

// ── Datos de ejemplo ─────────────────────────────────────────────────────────
function inicializarDatos() {
  // Categorías (Composite)
  store.categorias = [
    { id: 1, nombre: 'Electrónica',  padreId: null },
    { id: 2, nombre: 'Celulares',    padreId: 1    },
    { id: 3, nombre: 'Laptops',      padreId: 1    },
    { id: 4, nombre: 'Ropa',         padreId: null },
    { id: 5, nombre: 'Calzado',      padreId: 4    },
    { id: 6, nombre: 'Hogar',        padreId: null },
    { id: 7, nombre: 'Cocina',       padreId: 6    },
  ];

  store.productos = [
    { id: 1,  nombre: 'iPhone 15 Pro',    precio: 999.99,  stock: 8,  categoria: 'Celulares', emoji: '📱' },
    { id: 2,  nombre: 'Samsung Galaxy S24',precio: 849.99, stock: 5,  categoria: 'Celulares', emoji: '📱' },
    { id: 3,  nombre: 'Xiaomi Redmi Note',precio: 299.99,  stock: 12, categoria: 'Celulares', emoji: '📱' },
    { id: 4,  nombre: 'MacBook Air M2',   precio: 1299.99, stock: 3,  categoria: 'Laptops',   emoji: '💻' },
    { id: 5,  nombre: 'Dell XPS 15',      precio: 1099.99, stock: 4,  categoria: 'Laptops',   emoji: '💻' },
    { id: 6,  nombre: 'Remera Dry-Fit',   precio: 24.99,   stock: 50, categoria: 'Ropa',      emoji: '👕' },
    { id: 7,  nombre: 'Buzo Urbano',      precio: 49.99,   stock: 30, categoria: 'Ropa',      emoji: '🧥' },
    { id: 8,  nombre: 'Zapatillas Run',   precio: 79.99,   stock: 20, categoria: 'Calzado',   emoji: '👟' },
    { id: 9,  nombre: 'Sartén Antiadher.',precio: 34.99,   stock: 15, categoria: 'Cocina',    emoji: '🍳' },
    { id: 10, nombre: 'Set Cuchillos',    precio: 59.99,   stock: 7,  categoria: 'Cocina',    emoji: '🔪' },
  ];

  // Usuario admin precargado
  store.usuarios.push({
    id: store.nextUserId++,
    nombre: 'Admin', apellido: 'Sistema',
    email: 'admin@emarket.com', pass: 'admin123',
    rol: 'admin', legajo: 1001,
  });
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
      <div class="product-img">${p.emoji}</div>
      <div class="product-body">
        <div class="product-name">${p.nombre}</div>
        <div class="product-cat">${p.categoria}</div>
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
  const raices = store.categorias.filter(c => !c.padreId);
  const ul = document.getElementById('categoriaList');
  const sel = document.getElementById('searchCategory');

  // sidebar
  ul.innerHTML = `<li class="active" onclick="filtrarPorCategoria(null, this)">Todos</li>`;
  raices.forEach(cat => {
    ul.innerHTML += `<li onclick="filtrarPorCategoria('${cat.nombre}', this)">${cat.nombre}</li>`;
    const hijos = store.categorias.filter(c => c.padreId === cat.id);
    hijos.forEach(h => {
      ul.innerHTML += `<li style="padding-left:1.5rem;font-size:.88rem" onclick="filtrarPorCategoria('${h.nombre}', this)">↳ ${h.nombre}</li>`;
    });
  });

  // select de búsqueda
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
    renderCatalogo(store.productos.filter(p => p.categoria === categoria));
  }
  mostrarSeccion('catalogo');
}

// ── Búsqueda ─────────────────────────────────────────────────────────────────
function buscarProductos() {
  const texto    = document.getElementById('searchInput').value.toLowerCase().trim();
  const cat      = document.getElementById('searchCategory').value;
  const minVal   = parseFloat(document.getElementById('priceMin').value);
  const maxVal   = parseFloat(document.getElementById('priceMax').value);

  let resultado = store.productos;
  if (texto)              resultado = resultado.filter(p => p.nombre.toLowerCase().includes(texto));
  if (cat)                resultado = resultado.filter(p => p.categoria === cat);
  if (!isNaN(minVal))     resultado = resultado.filter(p => p.precio >= minVal);
  if (!isNaN(maxVal))     resultado = resultado.filter(p => p.precio <= maxVal);

  document.getElementById('catalogoTitulo').textContent =
    texto || cat ? `Resultados de búsqueda` : 'Todos los productos';
  mostrarSeccion('catalogo');
  renderCatalogo(resultado);
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
      <div class="cart-item-icon">${i.producto.emoji}</div>
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
  } else {
    renderCarrito();
    panel.classList.add('open');
    overlay.classList.remove('hidden');
  }
}

function cerrarTodo() {
  document.getElementById('carritoPanel').classList.remove('open');
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

  // Validaciones Strategy
  let nombreMetodo = '';
  if (store.metodoSeleccionado === 'tarjeta') {
    const num = document.getElementById('cardNumero').value.trim();
    const cvv = document.getElementById('cardCVV').value.trim();
    if (num.length < 16 || !/^\d+$/.test(num)) {
      mostrarError('checkoutError', 'Número de tarjeta inválido (16 dígitos).'); return;
    }
    if (cvv.length < 3 || !/^\d+$/.test(cvv)) {
      mostrarError('checkoutError', 'CVV inválido.'); return;
    }
    nombreMetodo = 'Tarjeta de Crédito';
  } else if (store.metodoSeleccionado === 'paypal') {
    const cuenta = document.getElementById('ppCuenta').value.trim();
    if (!cuenta.includes('@')) {
      mostrarError('checkoutError', 'Email de PayPal inválido.'); return;
    }
    nombreMetodo = 'PayPal';
  } else if (store.metodoSeleccionado === 'transferencia') {
    const cbu   = document.getElementById('transCBU').value.trim();
    const banco = document.getElementById('transBanco').value.trim();
    if (cbu.length < 22 || !/^\d+$/.test(cbu)) {
      mostrarError('checkoutError', 'CBU inválido (22 dígitos).'); return;
    }
    if (!banco) {
      mostrarError('checkoutError', 'Ingresá el nombre del banco.'); return;
    }
    nombreMetodo = 'Transferencia Bancaria';
  }

  // Verificar stock antes de confirmar
  for (const item of store.carrito) {
    if (item.producto.stock < item.cantidad) {
      mostrarError('checkoutError', `Stock insuficiente para ${item.producto.nombre}.`);
      return;
    }
  }

  // Crear pedido (State pattern: inicia en Pendiente, avanza a Pagado)
  const pedido = {
    id: store.nextPedidoId++,
    clienteId: store.usuarioActual.id,
    fecha: new Date().toLocaleDateString('es-AR'),
    estado: 'Pendiente',
    metodoPago: nombreMetodo,
    items: store.carrito.map(i => ({ ...i })),
    total: calcularTotalCarrito(),
  };

  // Descontar stock
  store.carrito.forEach(i => { i.producto.stock -= i.cantidad; });

  // Avanzar a Pagado (pago procesado)
  pedido.estado = 'Pagado';

  store.pedidos.push(pedido);
  store.carrito = [];
  actualizarBadgeCarrito();
  cerrarModal('modalCheckout');

  // Observer: notificación de confirmación
  notificarCambioEstado(pedido);
  toast(`¡Pedido #${pedido.id} confirmado! Pago con ${nombreMetodo} procesado. ✓`, 'success');

  renderCatalogo(store.productos);
}

// ── Pedidos ──────────────────────────────────────────────────────────────────
function renderMisPedidos() {
  const pedidos = store.pedidos.filter(p => p.clienteId === store.usuarioActual?.id);
  const el = document.getElementById('misPedidosList');
  if (!pedidos.length) {
    el.innerHTML = '<p style="color:var(--muted)">No tenés pedidos todavía.</p>';
    return;
  }
  el.innerHTML = pedidos.map(p => renderPedidoCard(p, false)).join('');
}

function renderAdminPedidos() {
  const el = document.getElementById('todosLosPedidosList');
  if (!store.pedidos.length) {
    el.innerHTML = '<p style="color:var(--muted)">No hay pedidos registrados.</p>';
    return;
  }
  el.innerHTML = store.pedidos.map(p => renderPedidoCard(p, true)).join('');
}

function renderPedidoCard(pedido, esAdmin) {
  const sigEstado = siguienteEstado(pedido.estado);
  const itemsTexto = pedido.items.map(i => `${i.producto.nombre} x${i.cantidad}`).join(', ');
  const cliente = store.usuarios.find(u => u.id === pedido.clienteId);
  const clienteNombre = cliente ? `${cliente.nombre} ${cliente.apellido}` : '—';

  const btnAvanzar = esAdmin && sigEstado
    ? `<button onclick="avanzarEstadoPedido(${pedido.id})">→ ${sigEstado}</button>`
    : '';

  return `
    <div class="pedido-card" id="pedido-${pedido.id}">
      <div class="pedido-header">
        <div>
          <span class="pedido-id">Pedido #${pedido.id}</span>
          ${esAdmin ? `<span style="color:var(--muted);font-size:.85rem;margin-left:.5rem">· ${clienteNombre}</span>` : ''}
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

function siguienteEstado(estadoActual) {
  const idx = ESTADOS.indexOf(estadoActual);
  return idx >= 0 && idx < ESTADOS.length - 1 ? ESTADOS[idx + 1] : null;
}

function avanzarEstadoPedido(pedidoId) {
  const pedido = store.pedidos.find(p => p.id === pedidoId);
  if (!pedido) return;
  const sig = siguienteEstado(pedido.estado);
  if (!sig) return;
  pedido.estado = sig;

  // Observer: notificar al cliente
  notificarCambioEstado(pedido);

  renderAdminPedidos();
  toast(`Pedido #${pedido.id} actualizado a "${sig}"`, 'success');
}

function notificarCambioEstado(pedido) {
  const cliente = store.usuarios.find(u => u.id === pedido.clienteId);
  if (cliente) {
    console.log(`[EMAIL -> ${cliente.email}] Pedido #${pedido.id} cambió a: ${pedido.estado}`);
  }
}

// ── Auth ─────────────────────────────────────────────────────────────────────
function registrar() {
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
  if (store.usuarios.find(u => u.email === email)) {
    mostrarError('regError', 'Ya existe una cuenta con ese email.'); return;
  }
  if (rol === 'admin' && !legajo) {
    mostrarError('regError', 'El legajo es obligatorio para administradores.'); return;
  }

  const nuevo = { id: store.nextUserId++, nombre, apellido, email, pass, rol, legajo };
  store.usuarios.push(nuevo);
  cerrarModal('modalRegistro');
  iniciarSesion(nuevo);
  toast(`Bienvenido/a, ${nombre}! Cuenta creada. ✓`, 'success');
}

function login() {
  ocultarError('loginError');
  const email = document.getElementById('loginEmail').value.trim().toLowerCase();
  const pass  = document.getElementById('loginPass').value;

  const usuario = store.usuarios.find(u => u.email === email && u.pass === pass);
  if (!usuario) {
    mostrarError('loginError', 'Email o contraseña incorrectos.'); return;
  }
  cerrarModal('modalLogin');
  iniciarSesion(usuario);
  toast(`Bienvenido/a, ${usuario.nombre}!`, 'success');
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
inicializarDatos();
renderCategoriasMenu();
renderCatalogo(store.productos);
