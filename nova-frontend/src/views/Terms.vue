<template>
  <div class="min-h-screen bg-bg-base p-6 flex items-center justify-center">
    <div class="w-full max-w-3xl bg-bg-surface rounded-3xl border border-border-subtle shadow-float p-7">
      <h1 class="text-2xl font-bold text-text-primary">用户协议</h1>
      <p class="mt-2 text-sm text-text-secondary">以下为占位文本，请在上线前替换为你的正式协议内容。</p>

      <div class="mt-6 space-y-4 text-sm text-text-secondary leading-relaxed">
        <p>1. 你在使用本服务时，应遵守相关法律法规。</p>
        <p>2. 你需要保证提供的信息真实有效，并对账号安全负责。</p>
        <p>3. 本服务将采取必要的技术措施保护你的数据安全，但无法保证任何情况下的绝对安全。</p>
        <p>4. 我们可能会根据产品迭代更新协议内容，更新后以页面展示为准。</p>
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

