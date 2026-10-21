<script setup>
import NavHeader from '@/views/NavHeader.vue'
import NavFooter from '@/views/NavFooter.vue'
import { RouterView } from 'vue-router';
import NavServiceBar from '@/views/NavServiceBar.vue'
</script>

<template>
    <NavHeader></NavHeader>
    <div style="width: 1800px;margin: 0 auto;">
        <RouterView></RouterView>
    </div>
    <NavServiceBar></NavServiceBar>
    <NavFooter></NavFooter>
</template>