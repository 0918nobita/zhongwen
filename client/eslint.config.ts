import js from '@eslint/js';
import { defineConfig } from 'eslint/config';
import oxlint from 'eslint-plugin-oxlint';
import vue from 'eslint-plugin-vue';
import ts from 'typescript-eslint';

export default defineConfig(
  {
    ignores: [
      'dist',
      'node_modules',
      'src-tauri',
    ],
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
    rules: {
      'vue/html-self-closing': ['error', {
        html: {
          void: 'always',
          normal: 'always',
          component: 'always',
        },
      }],
      'vue/max-attributes-per-line': ['error', {
        singleline: 5,
        multiline: 1,
      }],
      'vue/singleline-html-element-content-newline': 'off',
    },
  },
  ...oxlint.configs['flat/all'],
);
