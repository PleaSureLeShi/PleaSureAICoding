declare namespace API {
  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseChatMessageVO = {
    code?: number
    data?: ChatMessageVO
    message?: string
  }

  type BaseResponseChatRoomVO = {
    code?: number
    data?: ChatRoomVO
    message?: string
  }

  type BaseResponseListChatRoomVO = {
    code?: number
    data?: ChatRoomVO[]
    message?: string
  }

  type BaseResponseListRoomMemberVO = {
    code?: number
    data?: RoomMemberVO[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageChatMessageVO = {
    code?: number
    data?: PageChatMessageVO
    message?: string
  }

  type BaseResponsePageChatRoomVO = {
    code?: number
    data?: PageChatRoomVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type ChatMessageQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    messageType?: string
    roomId?: number
    senderId?: number
    receiverId?: number
    lastMessageTime?: string
  }

  type ChatMessageSendRequest = {
    messageType: string
    roomId?: number
    receiverId?: number
    contentType?: string
    content: string
    replyToId?: number
  }

  type ChatMessageVO = {
    id?: number
    messageType?: string
    roomId?: number
    senderId?: number
    senderName?: string
    senderAvatar?: string
    receiverId?: number
    contentType?: string
    content?: string
    fileUrl?: string
    fileName?: string
    fileSize?: number
    replyToId?: number
    isRecalled?: number
    sendTime?: string
  }

  type ChatRoomCreateRequest = {
    roomName: string
    roomDescription?: string
    roomType: string
    maxMembers: number
    isPublic: number
    password?: string
  }

  type ChatRoomJoinRequest = {
    roomId: number
    password?: string
  }

  type ChatRoomQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    roomName?: string
    roomType?: string
    status?: number
    isPublic?: number
    ownerId?: number
  }

  type ChatRoomVO = {
    id?: number
    roomName?: string
    roomDescription?: string
    roomType?: string
    maxMembers?: number
    currentMembers?: number
    ownerId?: number
    ownerName?: string
    isPublic?: number
    status?: number
    isJoined?: boolean
    createTime?: string
  }

  type chatToGenCodeParams = {
    appId: number
    message: string
  }

  type DeleteRequest = {
    id?: number
  }

  type downloadAppCodeParams = {
    appId: number
  }

  type getAppVOByIdByAdminParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getChatMessageParams = {
    messageId: number
  }

  type getChatRoomParams = {
    roomId: number
  }

  type getRoomMembersParams = {
    roomId: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type leaveRoomParams = {
    roomId: number
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatMessageVO = {
    records?: ChatMessageVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatRoomVO = {
    records?: ChatRoomVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type recallMessageParams = {
    messageId: number
  }

  type RoomMemberVO = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    role?: string
    joinTime?: string
    lastReadTime?: string
    isMuted?: number
    mutedUntil?: string
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserSelfUpdateRequest = {
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserSetRoleRequest = {
    id?: number
    userRole?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
