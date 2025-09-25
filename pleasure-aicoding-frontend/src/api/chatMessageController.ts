// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /chatMessage/get/${param0} */
export async function getChatMessage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getChatMessageParams,
  options?: { [key: string]: any }
) {
  const { messageId: param0, ...queryParams } = params
  return request<API.BaseResponseChatMessageVO>(`/chatMessage/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatMessage/private/list/page */
export async function listPrivateMessageVoByPage(
  body: API.ChatMessageQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatMessageVO>('/chatMessage/private/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatMessage/recall/${param0} */
export async function recallMessage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.recallMessageParams,
  options?: { [key: string]: any }
) {
  const { messageId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/chatMessage/recall/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatMessage/room/list/page */
export async function listRoomMessageVoByPage(
  body: API.ChatMessageQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatMessageVO>('/chatMessage/room/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatMessage/send */
export async function sendMessage(
  body: API.ChatMessageSendRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/chatMessage/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
