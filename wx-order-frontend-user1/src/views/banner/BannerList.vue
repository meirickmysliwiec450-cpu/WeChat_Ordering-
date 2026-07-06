<template>
  <div>
    <h3>轮播图管理</h3>
    <el-card>
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
      </div>
      <el-table :data="banners" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="图片预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              style="width: 80px; height: 50px; border-radius: 4px"
              fit="cover"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="imageUrl"
          label="图片URL"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-button size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? "禁用" : "启用" }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      v-model="dialogVisible"
      width="800px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"
          ><el-input v-model="form.title" placeholder="请输入轮播图标题"
        /></el-form-item>
        <el-form-item label="图片URL">
          <el-input
            v-model="form.imageUrl"
            placeholder="选择下方图片或手动输入URL"
          />
        </el-form-item>
        <!-- 从菜品库选择图片 -->
        <el-form-item label="菜品图片">
          <div class="image-picker">
            <div
              v-if="dishList.length === 0"
              style="color: #909399; font-size: 13px; margin-bottom: 8px"
            >
              暂无菜品，请先在菜品管理中上架菜品
            </div>
            <div class="image-grid">
              <div
                v-for="dish in dishList"
                :key="dish.id"
                class="image-item"
                :class="{ selected: form.imageUrl === dish.image }"
                @click="selectDish(dish)"
              >
                <el-image
                  :src="dish.image"
                  style="width: 100%; height: 80px"
                  fit="cover"
                />
                <span class="image-name">{{ dish.dishName }}</span>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="跳转链接"
          ><el-input v-model="form.linkUrl" placeholder="可选"
        /></el-form-item>
        <el-form-item label="排序"
          ><el-input-number v-model="form.sort" :min="0"
        /></el-form-item>
        <el-form-item label="备注"
          ><el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="可选"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import {
  getBanners,
  addBanner,
  updateBanner,
  updateBannerStatus,
  deleteBanner,
} from "@/api/banner";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";

const banners = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const editId = ref(null);
const form = ref({ title: "", imageUrl: "", linkUrl: "", sort: 0, remark: "" });
const dishList = ref([]);

async function fetchData() {
  loading.value = true;
  try {
    const res = await getBanners();
    banners.value = res.data || [];
  } finally {
    loading.value = false;
  }
}

async function fetchDishes() {
  try {
    const token = localStorage.getItem("adminToken");
    const res = await axios.get("http://localhost:8080/api/admin/dishes", {
      params: { page: 1, pageSize: 100 },
      headers: { Authorization: `Bearer ${token}` },
    });
    dishList.value = (res.data.data?.list || []).filter((d) => d.status === 1);
  } catch (e) {
    /* 忽略 */
  }
}

function relativePath(url) {
  if (!url) return "";
  const idx = url.indexOf("/images/");
  return idx >= 0 ? url.substring(idx) : url;
}

function selectDish(dish) {
  form.value.imageUrl = relativePath(dish.image);
  form.value.title = form.value.title || dish.dishName;
}

function handleAdd() {
  isEdit.value = false;
  editId.value = null;
  form.value = { title: "", imageUrl: "", linkUrl: "", sort: 0, remark: "" };
  fetchDishes();
  dialogVisible.value = true;
}

function handleEdit(row) {
  isEdit.value = true;
  editId.value = row.id;
  form.value = {
    title: row.title,
    imageUrl: row.imageUrl,
    linkUrl: row.linkUrl,
    sort: row.sort,
    remark: row.remark,
  };
  fetchDishes();
  dialogVisible.value = true;
}

async function handleSave() {
  isEdit.value
    ? await updateBanner(editId.value, form.value)
    : await addBanner(form.value);
  ElMessage.success(isEdit.value ? "修改成功" : "新增成功");
  dialogVisible.value = false;
  fetchData();
}

async function handleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1;
  await updateBannerStatus(row.id, newStatus);
  ElMessage.success("状态更新");
  row.status = newStatus;
}

async function handleDelete(row) {
  await ElMessageBox.confirm("确定删除该轮播图吗？", "确认", {
    type: "warning",
  });
  await deleteBanner(row.id);
  ElMessage.success("删除成功");
  fetchData();
}

onMounted(fetchData);
</script>

<style scoped>
.image-picker {
  width: 100%;
}
.folder-label {
  font-size: 13px;
  color: #409eff;
  font-weight: bold;
  margin: 8px 0 4px;
  padding-bottom: 4px;
  border-bottom: 1px solid #e8e8e8;
}
.image-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.image-item {
  width: 110px;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  transition: border-color 0.2s;
  text-align: center;
}
.image-item:hover {
  border-color: #409eff;
}
.image-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}
.image-name {
  display: block;
  font-size: 11px;
  color: #606266;
  padding: 2px 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.image-price {
  display: block;
  font-size: 11px;
  color: #e04030;
  font-weight: bold;
}
</style>
