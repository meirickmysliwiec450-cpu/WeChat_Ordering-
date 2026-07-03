Component({
  properties: {
    dish: { type: Object, value: {} }
  },
  methods: {
    onAdd() {
      const d = this.data.dish
      this.triggerEvent('add', {
        id: d.id,
        dish: { id: d.id, name: d.dishName || d.name, price: d.price, image: d.image || d.imageUrl }
      })
    },
    onDetail() {
      this.triggerEvent('detail', { id: this.data.dish.id })
    },
    onImgError() {
      this.setData({ 'dish.image': '' })
    }
  }
})
