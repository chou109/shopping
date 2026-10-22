export const buildCategoryTree = categories => {
  const categoryMap = {}
  const tree = []

  categories.forEach(category => {
    categoryMap[category.id] = { ...category, children: [] }
  })

  categories.forEach(category => {
    const node = categoryMap[category.id]
    const parent = categoryMap[category.parentId]
    if (category.parentId == null || category.parentId === 0 || !parent) {
      tree.push(node)
    } else {
      parent.children.push(node)
    }
  })

  return tree
}

const getParentPath = (tree, categoryId, path = []) => {
  for (const node of tree) {
    if (String(node.id) === String(categoryId)) return path
    const childPath = getParentPath(node.children || [], categoryId, [...path, node.name])
    if (childPath !== null) return childPath
  }
  return null
}

export const buildCategoryOptions = tree => {
  const result = []
  const visit = (categories, level = 0) => {
    categories.forEach(category => {
      const children = category.children || []
      if (children.length === 0) {
        const parents = getParentPath(tree, category.id) || []
        result.push({
          id: category.id,
          name: parents.length ? `${parents.join(' / ')} / ${category.name}` : category.name,
          originalName: category.name,
          level
        })
      } else {
        visit(children, level + 1)
      }
    })
  }
  visit(tree)
  return result
}

export const getCategoryPageRows = response => {
  if (Array.isArray(response?.rows)) return response.rows
  if (Array.isArray(response?.data)) return response.data
  return []
}

export const getCategoryPageTotal = response => {
  const total = Number(response?.total)
  return Number.isFinite(total) && total >= 0 ? total : getCategoryPageRows(response).length
}

export const fetchAllCategoryPages = async (fetchPage, filters = {}, pageSize = 500) => {
  const allRows = []
  let pageNum = 1
  let total = Infinity

  while (allRows.length < total) {
    const response = await fetchPage({ ...filters, pageNum, pageSize })
    if (response?.code !== 200) throw new Error(response?.msg || '加载分类失败')
    const pageRows = getCategoryPageRows(response)
    total = getCategoryPageTotal(response)
    allRows.push(...pageRows)
    if (pageRows.length === 0) break
    pageNum += 1
  }

  return allRows
}
