/// <reference types="vite/client" />

declare module "*.vue" {
  import type { DefineComponent } from "vue";

  // oxlint-disable-next-line ban-types no-empty-object-type no-explicit-any
  const component: DefineComponent<{}, {}, any>;

  export default component;
}
