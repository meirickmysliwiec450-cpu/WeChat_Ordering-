const { getToken } = require('../../utils/auth')

Page({
  data: { groupedList: [], totalCount: 0, loading: true },

  onShow() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/payments`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          const list = res.data.data || []
          // 按日期分组
          const groups = {}
          list.forEach(item => {
            const date = (item.payTime || item.createTime || '').substring(0, 10)
            if (!groups[date]) groups[date] = { records: [], dayTotal: 0 }
            groups[date].records.push(item)
            groups[date].dayTotal += item.payAmount || 0
          })
          const groupedList = Object.keys(groups).sort((a, b) => b.localeCompare(a)).map(date => ({
            date, records: groups[date].records, dayTotal: groups[date].dayTotal.toFixed(2)
          }))
          this.setData({ groupedList, totalCount: list.length, loading: false })
        } else {
          this.setData({ loading: false })
        }
      },
      fail: () => this.setData({ loading: false })
    })
  }
})
