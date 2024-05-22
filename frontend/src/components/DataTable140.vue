<template>
  <div class="data-table">
    <div class="table-header">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Search..."
        class="search-input"
      />
    </div>
    <table>
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            @click="sortBy(col.key)"
            class="sortable"
          >
            {{ col.label }}
            <span v-if="sortKey === col.key">
              {{ sortOrder === 'asc' ? '\u25B2' : '\u25BC' }}
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in filteredData" :key="row.id">
          <td v-for="col in columns" :key="col.key">
            {{ row[col.key] }}
          </td>
        </tr>
      </tbody>
    </table>
    <div class="table-footer">
      <span>Showing {{ filteredData.length }} of {{ data.length }} items</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataTable140',
  props: {
    columns: {
      type: Array,
      required: true,
    },
    data: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      searchQuery: '',
      sortKey: '',
      sortOrder: 'asc',
    };
  },
  computed: {
    filteredData() {
      let result = [...this.data];
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(row =>
          this.columns.some(col =>
            String(row[col.key]).toLowerCase().includes(query)
          )
        );
      }
      if (this.sortKey) {
        result.sort((a, b) => {
          const modifier = this.sortOrder === 'asc' ? 1 : -1;
          if (a[this.sortKey] < b[this.sortKey]) return -1 * modifier;
          if (a[this.sortKey] > b[this.sortKey]) return 1 * modifier;
          return 0;
        });
      }
      return result;
    },
  },
  methods: {
    sortBy(key) {
      if (this.sortKey === key) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
      } else {
        this.sortKey = key;
        this.sortOrder = 'asc';
      }
    },
  },
};
</script>

<style scoped>
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 12px;
  width: 250px;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  padding: 10px 14px;
  text-align: left;
  border-bottom: 1px solid #eee;
}
th.sortable {
  cursor: pointer;
  user-select: none;
}
th.sortable:hover {
  background-color: #f5f5f5;
}
</style>
