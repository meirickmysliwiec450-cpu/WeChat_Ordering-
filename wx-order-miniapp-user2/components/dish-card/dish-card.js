Component({
  properties: {
    dish: {
      type: Object,
      value: {}
    }
  },

  methods: {
    onAdd() {
      this.triggerEvent('add', { 
        id: this.data.dish.id,
        dish: this.data.dish 
      })
    },

    onDetail() {
      this.triggerEvent('detail', { id: this.data.dish.id })
    },

    onImgError() {
      this.setData({ 'dish.imageUrl': '' })
    }
  }
})