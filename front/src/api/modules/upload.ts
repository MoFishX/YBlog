import request from '../request'
import type { ApiResponse } from '@/types/api'

export interface UploadResult {
  url: string
  filename: string
  size: number
}

export const uploadApi = {
  upload(file: File, type: string = 'avatar'): Promise<ApiResponse<UploadResult>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)
    return request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}