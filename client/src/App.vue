<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core';
import * as v from 'valibot';
import { onMounted, ref } from 'vue';

const GreetMsg = v.tuple([v.string(), v.string()]);

const lines = ref<string[]>([]);

onMounted(async () => {
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
</template>
