export const buildClientHeaders = port => ({
  'X-Client-Type': 'admin',
  'X-Client-Port': port || '80'
})
