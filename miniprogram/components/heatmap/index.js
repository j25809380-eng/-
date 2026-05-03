const { request } = require('../../utils/request');

const COLORS = ['#1a1a1a', '#0e4429', '#006d32', '#26a641', '#39d353'];
const BOX_SIZE = 12;
const BOX_GAP = 3;
const BOX_STEP = BOX_SIZE + BOX_GAP;
const LABEL_W = 32;
const HEADER_H = 20;
const MONTH_LABELS = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];

Component({
  properties: {
    days: {
      type: Number,
      value: 84
    }
  },

  data: {
    canvasWidth: 360,
    canvasHeight: 140
  },

  lifetimes: {
    attached() {
      this.loadData();
    }
  },

  methods: {
    loadData() {
      request({
        url: '/analytics/heatmap',
        data: { days: this.properties.days }
      }).then((data) => {
        this.renderHeatmap(data || []);
      }).catch(() => {
        this.renderHeatmap([]);
      });
    },

    renderHeatmap(data) {
      const query = this.createSelectorQuery();
      query.select('#heatmap-canvas')
        .fields({ node: true, size: true })
        .exec((res) => {
          if (!res || !res[0] || !res[0].node) return;
          const canvas = res[0].node;
          const ctx = canvas.getContext('2d');
          const dpr = wx.getSystemInfoSync().pixelRatio;

          const totalWeeks = Math.ceil(this.properties.days / 7);
          const w = LABEL_W + totalWeeks * BOX_STEP + 16;
          const h = HEADER_H + 7 * BOX_STEP + 12;

          canvas.width = w * dpr;
          canvas.height = h * dpr;
          ctx.scale(dpr, dpr);

          // 构建日期→count映射
          const countMap = {};
          (data || []).forEach((item) => {
            countMap[item.date] = item.count || 0;
          });

          const today = new Date();
          today.setHours(0, 0, 0, 0);
          const startDate = new Date(today);
          startDate.setDate(startDate.getDate() - this.properties.days + 1);

          // 背景
          ctx.fillStyle = '#131313';
          ctx.fillRect(0, 0, w, h);

          // 月份标签
          ctx.fillStyle = '#707363';
          ctx.font = '10px sans-serif';
          ctx.textAlign = 'left';
          let lastMonth = -1;
          for (let d = new Date(startDate); d <= today; d.setDate(d.getDate() + 1)) {
            const month = d.getMonth();
            if (month !== lastMonth) {
              const dayOffset = Math.floor((d - startDate) / (1000 * 60 * 60 * 24));
              const weekIndex = Math.floor(dayOffset / 7);
              const x = LABEL_W + weekIndex * BOX_STEP;
              ctx.fillText(MONTH_LABELS[month], x, 12);
              lastMonth = month;
            }
          }

          // 绘制格子
          for (let d = new Date(startDate); d <= today; d.setDate(d.getDate() + 1)) {
            const dayOffset = Math.floor((d - startDate) / (1000 * 60 * 60 * 24));
            const weekIndex = Math.floor(dayOffset / 7);
            const dayOfWeek = d.getDay();

            const x = LABEL_W + weekIndex * BOX_STEP;
            const y = HEADER_H + dayOfWeek * BOX_STEP;

            const dateStr = d.toISOString().slice(0, 10);
            const count = countMap[dateStr] || 0;

            let colorIndex = 0;
            if (count > 0) colorIndex = 1;
            if (count >= 2) colorIndex = 2;
            if (count >= 3) colorIndex = 3;
            if (count >= 4) colorIndex = 4;

            ctx.fillStyle = COLORS[colorIndex];
            ctx.beginPath();
            this.roundRect(ctx, x, y, BOX_SIZE, BOX_SIZE, 2);
            ctx.fill();
          }

          this.setData({ canvasWidth: w, canvasHeight: h });
        });
    },

    roundRect(ctx, x, y, w, h, r) {
      ctx.moveTo(x + r, y);
      ctx.lineTo(x + w - r, y);
      ctx.arcTo(x + w, y, x + w, y + r, r);
      ctx.lineTo(x + w, y + h - r);
      ctx.arcTo(x + w, y + h, x + w - r, y + h, r);
      ctx.lineTo(x + r, y + h);
      ctx.arcTo(x, y + h, x, y + h - r, r);
      ctx.lineTo(x, y + r);
      ctx.arcTo(x, y, x + r, y, r);
    }
  }
});
