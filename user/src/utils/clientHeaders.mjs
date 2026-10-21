export const buildClientHeaders = port => ({
  'X-Client-Type': 'user',
  'X-Client-Port': port || '80'
})
