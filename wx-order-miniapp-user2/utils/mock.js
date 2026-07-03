const categories = [
  { id: 'hot', name: '热销推荐' },
  { id: 'rice', name: '主食套餐' },
  { id: 'noodle', name: '面食粉类' },
  { id: 'snack', name: '小吃甜点' },
  { id: 'drink', name: '饮品汤品' }
]

const dishes = [
  {
    id: 'd001',
    categoryId: 'hot',
    name: '招牌红烧牛肉饭',
    desc: '精选牛腩慢炖，搭配时蔬和米饭',
    price: 22,
    sales: 318,
    stock: 80,
    imageColor: '#d94c2f',
    label: '招牌'
  },
  {
    id: 'd002',
    categoryId: 'hot',
    name: '香辣鸡腿饭',
    desc: '外酥里嫩，微辣开胃',
    price: 18,
    sales: 286,
    stock: 90,
    imageColor: '#e0802f',
    label: '热卖'
  },
  {
    id: 'd003',
    categoryId: 'rice',
    name: '鱼香肉丝盖饭',
    desc: '酸甜微辣，经典家常口味',
    price: 16,
    sales: 168,
    stock: 65,
    imageColor: '#b64d2f',
    label: '经典'
  },
  {
    id: 'd004',
    categoryId: 'rice',
    name: '番茄炒蛋饭',
    desc: '清爽下饭，适合少油轻食',
    price: 14,
    sales: 152,
    stock: 60,
    imageColor: '#e44d3a',
    label: '轻食'
  },
  {
    id: 'd005',
    categoryId: 'noodle',
    name: '重庆小面',
    desc: '麻辣鲜香，支持备注少辣',
    price: 13,
    sales: 201,
    stock: 75,
    imageColor: '#c72525',
    label: '麻辣'
  },
  {
    id: 'd006',
    categoryId: 'noodle',
    name: '酸汤肥牛粉',
    desc: '酸辣开胃，汤底浓郁',
    price: 20,
    sales: 139,
    stock: 45,
    imageColor: '#f0a72f',
    label: '新品'
  },
  {
    id: 'd007',
    categoryId: 'snack',
    name: '脆皮炸鸡块',
    desc: '现炸小食，可搭配饮品',
    price: 12,
    sales: 196,
    stock: 55,
    imageColor: '#d18435',
    label: '小吃'
  },
  {
    id: 'd008',
    categoryId: 'snack',
    name: '红糖糍粑',
    desc: '软糯香甜，餐后甜点',
    price: 10,
    sales: 88,
    stock: 40,
    imageColor: '#8b4f2f',
    label: '甜点'
  },
  {
    id: 'd009',
    categoryId: 'drink',
    name: '柠檬冰茶',
    desc: '清爽解腻，冰度可备注',
    price: 8,
    sales: 241,
    stock: 100,
    imageColor: '#f2c94c',
    label: '饮品'
  },
  {
    id: 'd010',
    categoryId: 'drink',
    name: '紫菜蛋花汤',
    desc: '营养热汤，适合套餐加购',
    price: 6,
    sales: 120,
    stock: 100,
    imageColor: '#6abf69',
    label: '汤品'
  }
]

const banners = [
  { title: '今日特惠', subTitle: '招牌套餐满 30 减 5' },
  { title: '快速点餐', subTitle: '选菜、下单、支付一站完成' }
]

module.exports = {
  categories,
  dishes,
  banners
}
