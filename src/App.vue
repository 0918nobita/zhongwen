<script setup lang="ts">
// import { invoke } from '@tauri-apps/api/core';
import { segment } from 'pinyin-pro';
import { onMounted, ref } from 'vue';

const pinyinSegments = ref<Array<{ result: string; origin: string }>>([]);

onMounted(() => {
  pinyinSegments.value = segment('病从口入，祸从口出。');
});
</script>

<template>
  <p>
    <ruby>
      <template v-for="(seg, index) in pinyinSegments" :key="index">
        <span class="chinese">{{ seg.origin }}</span>
        <rt>{{ seg.result }}</rt>
      </template>
    </ruby>
  </p>
</template>

<style scoped>
.chinese {
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}
</style>
