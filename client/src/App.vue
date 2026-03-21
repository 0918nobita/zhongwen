<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core';
import { html } from 'pinyin-pro';
import * as v from 'valibot';
import { onMounted, ref } from 'vue';

const GreetMsg = v.tuple([v.string(), v.string()]);

const lines = ref<string[]>([]);

const pinyinHtmlString = ref<string>('');

onMounted(async () => {
  pinyinHtmlString.value = html('病从口入，祸从口出。');

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
  <p>
    <template v-for="(line, index) in lines" :key="index">
      <br v-if="index > 0" />
      {{ line }}
    </template>
  </p>
  <div v-html="pinyinHtmlString" />
</template>
