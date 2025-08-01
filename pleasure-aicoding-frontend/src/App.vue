<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
// import { useLoginUserStore } from '@/stores/useLoginUserStore'
// import { healthUsingGet } from '@/api/mainController'
//
//已经在access.ts当中校验权限，这里注释掉，避免重复校验
// healthUsingGet().then((res) => {
//   console.log(res)
// })
//
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()
</script>

<style scoped>

</style>
