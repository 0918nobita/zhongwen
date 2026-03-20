<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core';
import { onMounted, ref } from 'vue';

const greetMsg = ref('something');

onMounted(async () => {
  greetMsg.value = await invoke('greet', { name: 'Example user' });
});
</script>

<template>
  <main class="container">
    <p>{{ greetMsg }}</p>
  </main>
</template>

<style scoped></style>
