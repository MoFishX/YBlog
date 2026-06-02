<template>
  <footer class="border-t border-gray-200 bg-white mt-12">
    <div class="container mx-auto px-4 py-6 text-center text-sm text-gray-500">
      <p>&copy; {{ year }} MyBlog. Powered by Vue3 + TypeScript.</p>
    </div>
  </footer>
</template>

<script setup lang="ts">
const year = new Date().getFullYear()
</script>
