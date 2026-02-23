import { defineConfig } from 'eslint/config';
import js from '@eslint/js';
import oxlint from 'eslint-plugin-oxlint';
import ts from 'typescript-eslint';
import vue from 'eslint-plugin-vue';

export default defineConfig(
  {
    ignores: ['src-tauri'],
  },
  js.configs.recommended,
  ts.configs.recommendedTypeChecked,
  {
    languageOptions: {
      parserOptions: {
        projectService: true,
      },
    },
  },
  vue.configs['flat/recommended'],
  {
    files: ['src/**/*.vue'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      parserOptions: {
        parser: ts.parser,
        extraFileExtensions: ['.vue'],
      },
    },
  },
  ...oxlint.configs['flat/all'],
);
