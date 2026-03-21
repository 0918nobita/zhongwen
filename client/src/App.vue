<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core';
import { segment } from 'pinyin-pro';
import * as v from 'valibot';
import { onMounted, ref } from 'vue';

const GreetMsg = v.tuple([v.string(), v.string()]);

const lines = ref<string[]>([]);

const pinyinSegments = ref<Array<{ result: string; origin: string }>>([]);

onMounted(async () => {
  pinyinSegments.value = segment('病从口入，祸从口出。');

  try {
    const res = await invoke('greet', { name: 'Kodai' });
    const parsed = v.parse(GreetMsg, res);
    lines.value = parsed;
  } catch {
    lines.value = ['エラーが発生しました'];
  }
});
</script>

<template>
  <p class="mx-2 my-1">
    <template v-for="(line, index) in lines" :key="index">
      <br v-if="index > 0" />
      {{ line }}
    </template>
  </p>
  <p class="mx-2 my-1">
    <ruby>
      <template v-for="(seg, index) in pinyinSegments" :key="index">
        <span class="font-chinese">{{ seg.origin }}</span>
        <rt class="text-sm">{{ seg.result }}</rt>
      </template>
    </ruby>
  </p>
  <button class="btn btn-primary mx-2 my-1">Click me</button>
</template>
