const { dishes } = require('./mock')

function getDiningType() {
  return wx.getStorageSync('diningType') || ''
}

function setDiningType(type) {
  wx.setStorageSync('diningType', type)
}

function getDiningTypeText(type = getDiningType()) {
  if (type === 'takeout') return '外送'
  if (type === 'pickup') return '自取'
  return '堂食'
}

function getCart() {
  return wx.getStorageSync('cart') || []
}

function setCart(cart) {
  wx.setStorageSync('cart', cart)
}

function findDish(id) {
  return dishes.find(item => item.id == id)
}

function addToCart(dishObj, count = 1) {
  if (!dishObj || !dishObj.id) {
    return getCart()
  }
  const dishId = dishObj.id
  const cart = getCart()
  const index = cart.findIndex(item => item.id == dishId)
  if (index > -1) {
    cart[index].count += count
  } else {
    cart.push({
      id: dishObj.id,
      name: dishObj.dishName,
      price: dishObj.price,
      image: dishObj.image || '',
      count,
      imageColor: dishObj.imageColor
    })
  }
  setCart(cart)
  return cart
}

function updateCartItem(dishId, count) {
  let cart = getCart()
  if (count <= 0) {
    cart = cart.filter(item => item.id != dishId)
  } else {
    cart = cart.map(item => item.id == dishId ? Object.assign({}, item, { count }) : item)
  }
  setCart(cart)
  return cart
}

function clearCart() {
  setCart([])
}

function getCartSummary(cart = getCart()) {
  return cart.reduce((summary, item) => {
    summary.totalCount += item.count
    summary.totalPrice += item.price * item.count
    return summary
  }, { totalCount: 0, totalPrice: 0 })
}

function createOrder(data) {
  const orders = wx.getStorageSync('orders') || []
  const now = new Date()
  const order = Object.assign({
    id: '' + now.getTime(),
    createTime: formatTime(now),
    status: 'paid',
    statusText: '已支付'
  }, data)
  orders.unshift(order)
  wx.setStorageSync('orders', orders)
  return order
}

function updateOrder(orderId, patch) {
  const orders = wx.getStorageSync('orders') || []
  const next = orders.map(item => item.id === orderId ? Object.assign({}, item, patch) : item)
  wx.setStorageSync('orders', next)
  return next
}

function removeOrder(orderId) {
  const orders = wx.getStorageSync('orders') || []
  const next = orders.filter(item => item.id !== orderId)
  wx.setStorageSync('orders', next)
  return next
}

function formatTime(date) {
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

module.exports = {
  getDiningType,
  setDiningType,
  getDiningTypeText,
  getCart,
  addToCart,
  updateCartItem,
  clearCart,
  getCartSummary,
  createOrder,
  updateOrder,
  removeOrder
}
