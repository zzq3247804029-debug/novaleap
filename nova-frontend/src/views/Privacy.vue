<template>
  <div class="min-h-screen bg-bg-base p-6 flex items-center justify-center">
    <div class="w-full max-w-3xl bg-bg-surface rounded-3xl border border-border-subtle shadow-float p-7">
      <h1 class="text-2xl font-bold text-text-primary">隐私政策</h1>
      <p class="mt-2 text-sm text-text-secondary">以下为占位文本，请在上线前替换为你的正式隐私政策内容。</p>

      <div class="mt-6 space-y-4 text-sm text-text-secondary leading-relaxed">
        <p>1. 我们仅在提供服务所必需的范围内收集和使用你的信息。</p>
        <p>2. 我们会采取合理的安全措施保护信息安全。</p>
        <p>3. 我们可能会使用必要的第三方服务以提升体验与稳定性。</p>
        <p>4. 你可以通过联系我们的方式申请更正或删除相关数据。</p>
      </div>

      <div class="mt-8 flex justify-end">
        <router-link
          to="/login"
          class="text-text-primary font-semibold hover:underline"
        >返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

