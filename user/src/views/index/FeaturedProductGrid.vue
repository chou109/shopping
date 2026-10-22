<template>
  <div class="featured-section">
    <div class="section-heading">
      <h2>{{ title }}</h2>
      <button v-if="showMore" type="button" @click="$emit('view-more')">查看全部 <span>›</span></button>
    </div>
    <div class="product-grid">
      <article v-for="product in products" :key="product.id" class="product-card" role="button" tabindex="0"
        :aria-label="`查看商品：${product.name}`" @click="$emit('select', product)"
        @keydown.enter.prevent="$emit('select', product)" @keydown.space.prevent="$emit('select', product)">
        <div class="product-image">
          <img :src="getProductImageUrl(product.mainImage || product.main_image)" :alt="product.name" @error="handleImageError" />
        </div>
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="subtitle">{{ product.subTitle || product.sub_title || '精选好物' }}</p>
          <strong>¥{{ formatPrice(product.price || product.currentUnitPrice || product.current_unit_price) }}</strong>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { formatProductPrice } from '@/utils/productDetail.mjs'

defineProps({
  title: { type: String, required: true },
  products: { type: Array, required: true },
  showMore: { type: Boolean, default: false }
})

defineEmits(['select', 'view-more'])

const formatPrice = formatProductPrice
</script>

<style scoped>
.featured-section {
  padding: 24px;
  border: 1px solid #f1e3da;
  border-radius: 12px;
  background: linear-gradient(135deg, #fffaf6 0%, #fff 72%);
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 0;
  color: #333;
  font-size: 22px;
  font-weight: 700;
}

.section-heading h2::before {
  display: inline-block;
  width: 4px;
  height: 21px;
  margin-right: 10px;
  border-radius: 3px;
  background: linear-gradient(180deg, #ff5500, #ff9900);
  content: '';
  vertical-align: -3px;
}

.section-heading button {
  border: 0;
  background: transparent;
  color: #777;
  cursor: pointer;
  font-size: 14px;
}

.section-heading button:hover {
  color: #f56623;
}

.section-heading button span {
  margin-left: 4px;
  font-size: 20px;
  vertical-align: -1px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.product-card {
  overflow: hidden;
  min-width: 0;
  border: 1px solid #f0f0f0;
  border-radius: 9px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 22px rgba(92, 59, 39, 0.12);
}

.product-card:focus-visible {
  outline: 3px solid #ff5500;
  outline-offset: 3px;
}

.product-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 190px;
  background: #fff;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-info {
  padding: 12px 14px 16px;
}

.product-info h3 {
  display: -webkit-box;
  overflow: hidden;
  min-height: 40px;
  margin: 0 0 5px;
  color: #333;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.subtitle {
  overflow: hidden;
  margin: 0 0 10px;
  color: #999;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-info strong {
  color: #e4393c;
  font-size: 18px;
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .featured-section {
    padding: 16px 12px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .product-image {
    height: 145px;
  }
}
</style>
