import { ref } from 'vue'

export function usePagination(defaultPageSize = 20) {
  const page = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)

  function reset() {
    page.value = 1
  }

  return { page, pageSize, total, reset }
}
