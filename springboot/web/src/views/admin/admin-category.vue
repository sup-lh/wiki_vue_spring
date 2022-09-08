<template>
  <a-layout>
    <p>
      <a-form>
        <a-form-model layout="inline" :model="param">
          <br>

          <a-form-model-item>
            <a-button
                type="primary"
                @click="handleQuery()"
                size="large"
            >
              刷新
            </a-button>
          </a-form-model-item>

          <a-form-model-item>
            <a-button
                type="primary"
                @click="add"
                size="large"
            >
              新增
            </a-button>
          </a-form-model-item>
          <br>

        </a-form-model>
      </a-form>
    </p>
    <a-layout-content :style="{ background: '#fff', padding: '44px', margin: 0, minHeight: '280px' }">
      <a-table
          :columns="columns"
          :row-key="record => record.id"
          :data-source="level"
          :pagination="false"
          :loading="loading">
        <template #cover="{ text: cover }">
          <img v-if="cover" :src="cover" alt="avatar"/>
        </template>
        <template v-slot:action="{ text, record }">
          <a-space size="small">
            <a-button type="primary" @click="edit(record)">
              编辑
            </a-button>

            <a-popconfirm
                title="是否继续进行删除操作？"
                ok-text="Yes"
                cancel-text="No"
                @confirm="del(record.id)"
            >
              <a-button type="danger">
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-layout-content>
  </a-layout>

  <!--  表单-->
  <a-modal
      v-model:visible="visible"
      title="表单"
      :confirm-loading="confirmLoading"
      @ok="handleOk"
  >

    <a-form :model="category_data" :label-col="labelCol" :wrapper-col="wrapperCol">
      <a-form-item label="名称">
        <a-input v-model:value="category_data.name"/>
      </a-form-item>
      <a-form-item label="父分类">
        <a-select v-model:value="category_data.parent">
          <a-select-option value="0">
            无
          </a-select-option>
          <a-select-option v-for="c in level" :key="c.id" :value="c.sort" :disabled="c.id == category_data.id">
            {{ c.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="顺序">
        <a-input v-model:value="category_data.sort"/>
      </a-form-item>
    </a-form>

  </a-modal>
</template>

<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue';
import axios from 'axios';
import {message} from "ant-design-vue";
import {Tool} from "@/util/tool";

export default defineComponent({
  name: 'Category',
  setup() {
    const param = ref();
    param.value = {};

    const loading = ref(false);

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
      },
      {
        title: '父分类',
        dataIndex: 'parent'
      },
      {
        title: '顺序',
        dataIndex: 'sort'
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }

    ];

    /**
     * 数据查询
     * 第一个参数为页数，第二个参数为条数
     **/

    const level = ref()
    // 在 编辑框 中调用的level中需要知道：
    //  :value 是返回值的设置，应该设置为sort，就是将拿到的这个树形结构中的这个选择的父分类的sort返回回去当作这个编辑内容的parent


    const handleQuery = () => {
      loading.value = true;
      axios.get("/category/allCategory").then((response) => {
        loading.value = false;
        const data = response.data;
        if (data.success) {
          level.value = Tool.array2Tree(data.content, 0)  // array2Tree处理显示树形结构的问题
        } else {
          message.error(data.message);
        }
      });
    };


    onMounted(() => {
      // 初始渲染，页数：1  ，条数：pagination的静态数据，3
      handleQuery();
    });


    // 表单
    const category_data = ref();
    const modalText = ref<string>('Content of the modal');
    const visible = ref<boolean>(false);
    const confirmLoading = ref<boolean>(false);

    const showModal = () => {
      visible.value = true;
    };


    // 点击ok键时的处理
    const handleOk = () => {
      confirmLoading.value = true;
      axios.post("/category/saveCategory", category_data.value).then((response) => {
        confirmLoading.value = false;
        if (response.data.success) {
          visible.value = false;
          handleQuery();
        } else {
          message.error(response.data.message)
        }
      });
    };

    // 修改数据
    const edit = (record: any) => {
      visible.value = true;
      category_data.value = Tool.copy(record)
    }
    // 新增
    const add = () => {
      visible.value = true;
      category_data.value = {}
    }
    // 删除
    const del = (id: number) => {
      axios.delete("/category/deleteCategory/" + id).then((response) => {
        if (response.data.success) {
          handleQuery();
        }
      });
    }


    return {
      param,
      columns,
      loading,
      handleQuery,

      // 表单
      modalText,
      visible,
      confirmLoading,
      showModal,
      handleOk,

      edit,
      add,
      del,

      category_data,
      level
    }
  }
});
</script>

<style scoped>
img {
  width: 50px;
  height: 50px;
}
</style>
