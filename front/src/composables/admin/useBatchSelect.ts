import { ref, computed } from 'vue'

export function useBatchSelect<T extends { id: number }>() {
  const selectedIds = ref<number[]>([])

  const hasSelection = computed(() => selectedIds.value.length > 0)

  function handleSelectionChange(rows: T[]) {
    selectedIds.value = rows.map((r) => r.id)
  }

  function clearSelection() {
    selectedIds.value = []
  }

  return { selectedIds, hasSelection, handleSelectionChange, clearSelection }
}
