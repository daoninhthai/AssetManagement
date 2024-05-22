<template>
  <span :class="['status-badge', statusClass]">
    {{ label }}
  </span>
</template>

<script>
export default {
  name: 'StatusBadge139',
  props: {
    status: {
      type: String,
      required: true,
    },
    customLabels: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    label() {
      return this.customLabels[this.status] || this.status;
    },
    statusClass() {
      const statusMap = {
        active: 'badge-success',
        completed: 'badge-success',
        pending: 'badge-warning',
        in_progress: 'badge-info',
        inactive: 'badge-secondary',
        failed: 'badge-danger',
        cancelled: 'badge-danger',
      };
      return statusMap[this.status.toLowerCase()] || 'badge-default';
    },
  },
};
</script>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: capitalize;
}
.badge-success { background-color: #d4edda; color: #155724; }
.badge-warning { background-color: #fff3cd; color: #856404; }
.badge-info { background-color: #d1ecf1; color: #0c5460; }
.badge-danger { background-color: #f8d7da; color: #721c24; }
.badge-secondary { background-color: #e2e3e5; color: #383d41; }
.badge-default { background-color: #f0f0f0; color: #333; }
</style>
