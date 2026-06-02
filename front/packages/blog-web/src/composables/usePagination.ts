import { ref } from 'vue'

export function usePagination(defaultPageSize = 10) {
  const page = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)

  function setTotal(t: number) {
    total.value = t
  }

  function reset() {
    page.value = 1
  }

  function onPageChange(p: number) {
    page.value = p
  }

  return { page, pageSize, total, setTotal, reset, onPageChange }
}
