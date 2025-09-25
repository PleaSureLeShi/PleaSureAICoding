// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /chatRoom/create */
export async function createChatRoom(
  body: API.ChatRoomCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/chatRoom/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /chatRoom/get/${param0} */
export async function getChatRoom(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getChatRoomParams,
  options?: { [key: string]: any }
) {
  const { roomId: param0, ...queryParams } = params
  return request<API.BaseResponseChatRoomVO>(`/chatRoom/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatRoom/join */
export async function joinRoom(body: API.ChatRoomJoinRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/chatRoom/join', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /chatRoom/joined */
export async function getUserJoinedRooms(options?: { [key: string]: any }) {
  return request<API.BaseResponseListChatRoomVO>('/chatRoom/joined', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatRoom/leave/${param0} */
export async function leaveRoom(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.leaveRoomParams,
  options?: { [key: string]: any }
) {
  const { roomId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/chatRoom/leave/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatRoom/list/page */
export async function listChatRoomVoByPage(
  body: API.ChatRoomQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatRoomVO>('/chatRoom/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
