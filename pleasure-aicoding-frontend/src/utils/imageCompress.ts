export interface AvatarCompressOptions {
  quality?: number // 压缩质量 0-1，默认0.8
  maxSize?: number // 最大尺寸（正方形），默认400px
  outputFormat?: string // 输出格式，默认jpeg
}

/**
 * 压缩头像图片
 * @param file 原始文件
 * @param options 压缩选项
 * @returns base64字符串
 */
export function compressAvatar(file: File, options: AvatarCompressOptions = {}): Promise<string> {
  return new Promise((resolve, reject) => {
    const {
      quality = 0.8,
      maxSize = 400,
      outputFormat = 'image/jpeg'
    } = options

    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()

    img.onload = () => {
      // 计算正方形尺寸（头像通常是正方形）
      const size = Math.min(img.width, img.height, maxSize)
      
      // 计算裁剪位置（居中裁剪）
      const sx = (img.width - size) / 2
      const sy = (img.height - size) / 2

      // 设置画布为正方形
      canvas.width = size
      canvas.height = size

      // 绘制裁剪后的图片
      ctx?.drawImage(img, sx, sy, size, size, 0, 0, size, size)

      // 转换为 base64
      const base64 = canvas.toDataURL(outputFormat, quality)
      resolve(base64)
    }

    img.onerror = () => reject(new Error('图片加载失败'))
    img.src = URL.createObjectURL(file)
  })
}

/**
 * 获取压缩后的大小估算
 */
export function getBase64Size(base64: string): number {
  // base64 编码后的大小约为原始大小的 4/3
  const padding = base64.endsWith('==') ? 2 : base64.endsWith('=') ? 1 : 0
  return Math.floor((base64.length - padding) * 3 / 4)
}