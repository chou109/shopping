export function getBaseUrl() {
    return import.meta.env.NODE_ENV === 'production' || import.meta.env.NODE_ENV === 'staging' ? import.meta.env.VITE_APP_BASE_URL : import.meta.env.VITE_APP_BASE_API
}