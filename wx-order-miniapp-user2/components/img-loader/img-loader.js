Component({
  properties: {
    src: { type: String, value: '' },
    mode: { type: String, value: 'aspectFill' },
    lazyLoad: { type: Boolean, value: false },
    style: { type: String, value: '' }
  },
  data: { localSrc: '' },
  lifetimes: {
    attached() { this.load() },
    ready() {},
  },
  observers: {
    'src': function() { this.load() }
  },
  methods: {
    load() {
      const src = this.properties.src
      if (!src) return
      // 已经是本地路径或base64直接显示
      if (src.startsWith('data:') || src.startsWith('wxfile:') || src.startsWith('/')) {
        this.setData({ localSrc: src }); return
      }
      // 微信真机通过downloadFile下载，模拟器直接显示http
      wx.downloadFile({
        url: src,
        success: res => {
          if (res.statusCode === 200) {
            this.setData({ localSrc: res.tempFilePath })
          }
        },
        fail: () => {
          // 下载失败用原始URL兜底
          this.setData({ localSrc: src })
        }
      })
    },
    onError() {
      // 加载失败不处理
    }
  }
})
