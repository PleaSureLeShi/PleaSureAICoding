<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'App',
  mounted() {
    window.addEventListener('storage', (event) => {
      if (event.key === 'user_info') {
        window.location.reload()
      }
    })
  }
}

</script>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background: #f0f2f5;
}
</style>