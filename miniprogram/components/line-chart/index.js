Component({
  properties: {
    canvasId: {
      type: String,
      value: 'lineChart'
    },
    labels: {
      type: Array,
      value: []
    },
    values: {
      type: Array,
      value: []
    },
    lineColor: {
      type: String,
      value: '#C3F400'
    },
    areaColor: {
      type: String,
      value: 'rgba(195,244,0,0.14)'
    },
    minValue: {
      type: Number,
      value: 0
    },
    maxValue: {
      type: Number,
      value: 0
    }
  },
  data: {
    canvasWidth: 320,
    canvasHeight: 130
  },
  lifetimes: {
    ready() {
      this.syncSize();
      this.draw();
    }
  },
  observers: {
    labels() {
      this.draw();
    },
    values() {
      this.draw();
    }
  },
  methods: {
    syncSize() {
      const system = wx.getSystemInfoSync();
      const width = Math.max(240, system.windowWidth - 84);
      const height = 130;
      this.setData({
        canvasWidth: width,
        canvasHeight: height
      });
    },

    draw() {
      const values = (this.data.values || []).map((item) => Number(item) || 0);
      if (!values.length) {
        return;
      }

      const ctx = wx.createCanvasContext(this.data.canvasId, this);
      const width = this.data.canvasWidth;
      const height = this.data.canvasHeight;
      const padding = 14;
      const chartHeight = height - padding * 2;
      const chartWidth = width - padding * 2;
      const min = this.data.minValue || Math.min(...values);
      const max = this.data.maxValue > min ? this.data.maxValue : Math.max(...values, min + 1);
      const diff = max - min;

      ctx.clearRect(0, 0, width, height);

      ctx.setLineWidth(1);
      ctx.setStrokeStyle('rgba(255,255,255,0.09)');
      for (let i = 0; i <= 4; i += 1) {
        const y = padding + (chartHeight / 4) * i;
        ctx.beginPath();
        ctx.moveTo(padding, y);
        ctx.lineTo(width - padding, y);
        ctx.stroke();
      }

      const stepX = values.length > 1 ? chartWidth / (values.length - 1) : 0;
      const points = values.map((value, index) => {
        const ratio = (value - min) / diff;
        const x = padding + stepX * index;
        const y = height - padding - ratio * chartHeight;
        return { x, y };
      });

      ctx.beginPath();
      points.forEach((point, index) => {
        if (index === 0) {
          ctx.moveTo(point.x, point.y);
        } else {
          ctx.lineTo(point.x, point.y);
        }
      });
      ctx.setStrokeStyle(this.data.lineColor);
      ctx.setLineWidth(2);
      ctx.stroke();

      if (points.length) {
        ctx.beginPath();
        ctx.moveTo(points[0].x, height - padding);
        points.forEach((point) => ctx.lineTo(point.x, point.y));
        ctx.lineTo(points[points.length - 1].x, height - padding);
        ctx.closePath();
        ctx.setFillStyle(this.data.areaColor);
        ctx.fill();
      }

      points.forEach((point) => {
        ctx.beginPath();
        ctx.arc(point.x, point.y, 2.6, 0, 2 * Math.PI);
        ctx.setFillStyle(this.data.lineColor);
        ctx.fill();
      });

      ctx.draw();
    }
  }
});
