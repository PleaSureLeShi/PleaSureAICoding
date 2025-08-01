<template>
  <div id="basicLayout">
    <a-layout style="height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://blog.csdn.net/m0_75262255?spm=1000.2115.3001.5343" target="_blank">杭州师范大学-信息科学与技术学院-软工222-PleaSure乐事</a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>
<script setup lang="ts">

import GlobalHeader from '@/components/GlobalHeader.vue'
</script>
<style scoped>
#basicLayout .content {
  background: linear-gradient(to right, #efefef, #fff);
  margin-bottom: 40px;
  padding: 20px;
  overflow: auto;
}
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  right: 0;
  left: 0;
  text-align: center;
}
#basicLayout .header{
  padding-inline: 20px;
  background: white;
  color: unset;
  margin-bottom: 16px;
}
</style>
