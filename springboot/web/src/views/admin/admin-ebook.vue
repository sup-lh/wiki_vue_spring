<template>
  <a-layout>
    <p>
      <a-form>
        <a-form-model layout="inline" :model="param">

          <a-form-model-item>
            <a-form>
              <a-input v-model:value="param.bookName" placeholder="名称">
              </a-input>
            </a-form>
          </a-form-model-item>

          <a-form-model-item>
            <a-button
                type="primary"
                @click="handleQuery({page:1,size:pagination.pageSize})"
            >
              查询
            </a-button>
          </a-form-model-item>

          <a-form-model-item>
            <a-button
                type="primary"
                @click="add"
            >
              新增
            </a-button>
          </a-form-model-item>

        </a-form-model>
      </a-form>
    </p>
    <a-layout-content :style="{ background: '#fff', padding: '44px', margin: 0, minHeight: '280px' }">
      <a-table
          :columns="columns"
          :row-key="record => record.id"
          :data-source="ebooks"
          :pagination="pagination"
          :loading="loading"
          @change="handleTableChange">

        <template #cover="{ text: cover }">
          <img v-if="cover" :src="cover" alt="avatar"/>
        </template>

        <template v-slot:category="{ text, record }">
          <span>{{ getCategoryName(record.category1Id) }} / {{ getCategoryName(record.category2Id) }}</span>
        </template>
        <template v-slot:action="{ text, record }">
          <a-space size="small">

            <router-link :to="'/admin-doc?ebookId=' + record.id">
              <a-button type="primary" @click="edit(record)">
                文档管理
              </a-button>
            </router-link>

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
      title="编辑"
      :confirm-loading="confirmLoading"
      @ok="handleOk"
  >

    <a-form :model="ebook_data" :label-col="labelCol" :wrapper-col="wrapperCol">
      <a-form-item label="封面">
        <a-input v-model:value="ebook_data.cover"/>
      </a-form-item>
      <a-form-item label="名称">
        <a-input v-model:value="ebook_data.bookName"/>
      </a-form-item>
      <a-form-item label="分类">
        <a-cascader
            v-model:value="categoryIds"
            :field-names="{ label: 'name', value: 'sort', children: 'children', title:'id'}"
            :options="level"
        />
      </a-form-item>
      <a-form-item label="描述">
        <a-input v-model:value="ebook_data.desCription"/>
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
  name: 'Ebook',
  setup() {
    const param = ref();
    param.value = {};

    const ebooks = ref();
    const pagination = ref({
      current: 1,
      pageSize: 4,
      total: 0
    });
    const loading = ref(false);

    const columns = [
      {
        title: '封面',
        dataIndex: 'cover',
        slots: {customRender: 'cover'}
      },
      {
        title: '名称',
        dataIndex: 'bookName'
      },
      {
        title: '分类',
        key: 'category1Id',
        slots: {customRender: 'category'}
      },
      {
        title: '文档数',
        dataIndex: 'docNum'
      },
      {
        title: '阅读数',
        dataIndex: 'viewNum'
      },
      {
        title: '点赞数',
        dataIndex: 'voteNum'
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
    const handleQuery = (params: any) => {
      loading.value = true;
      axios.get("/ebook/list", {
        params: {
          page: params.page,
          size: params.size,
          bookName: param.value.bookName
        }
      }).then((response) => {
        loading.value = false;
        const data = response.data;
        if (data.success) {
          ebooks.value = data.content.list;
          // 重置分页按钮
          // 动态修改pagination.value的current，total值，然后返回到:pagination
          pagination.value.current = params.page;
          pagination.value.total = data.content.total; // 总数一直是返回参数的参数条数
        } else {
          message.error(data.message);
        }
      });
    };


    // 表格点击页码时触发
    const handleTableChange = (pagination: any) => {
      handleQuery({
        page: pagination.current, // 按钮自动返回的参数pagination中的需要请求的页数
        size: pagination.pageSize// 按钮自动返回的参数pagination中的需要请求的条数
      });
    };


    // 表单
    const ebook_data = ref();
    const modalText = ref<string>('Content of the modal');
    const visible = ref<boolean>(false);
    const confirmLoading = ref<boolean>(false);

    const showModal = () => {
      visible.value = true;
    };

    // 存一二级分类 是一个数组
    const categoryIds = ref()

    // 点击ok键时的处理
    const handleOk = () => {
      confirmLoading.value = true;
      ebook_data.value.category1Id = categoryIds.value[0]
      ebook_data.value.category2Id = categoryIds.value[1]
      axios.post("/ebook/save", ebook_data.value).then((response) => {
        confirmLoading.value = false;
        if (response.data.success) {
          visible.value = false;
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        } else {
          message.error(response.data.message)
        }
      });
    };


    // 修改数据
    const edit = (record: any) => {
      visible.value = true;
      ebook_data.value = Tool.copy(record)
      categoryIds.value = [Number(ebook_data.value.category1Id), Number(ebook_data.value.category2Id)]
    }
    // 新增
    const add = () => {
      visible.value = true;
      ebook_data.value = {};
      categoryIds.value = []
    }
    // 删除
    const del = (id: number) => {
      axios.delete("/ebook/delete/" + id).then((response) => {
        if (response.data.success) {
          handleQuery({
            // 重新加载数据
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        }
      });
    }


    const level = ref();
    let categorys: any;
    const handleQueryCategory = () => {
      loading.value = true;
      axios.get("/category/allCategory").then((response) => {
        loading.value = false;
        const data = response.data;
        categorys = data.content;
        if (data.success) {

          level.value = Tool.array2Tree(data.content, 0)  // array2Tree处理显示树形结构的问题

          // 初始渲染，页数：1  ，条数：pagination的静态数据，3
          // 加载完分类再加载数据，顺序执行，不会报错
          handleQuery({
            page: 1,
            size: pagination.value.pageSize
          });

        } else {
          message.error(data.message);
        }
      });
    };

    const getCategoryName = (cid: number) => {
      let result = "";
      categorys.forEach((item: any) => {
        if (item.sort == cid) {
          // return item.name; // 注意，这里直接return不起作用
          result = item.name;
        }
      });
      return result;
    };

    onMounted(() => {
      handleQueryCategory();
    });


    return {
      param,
      ebooks,
      pagination,
      columns,
      loading,
      handleTableChange,
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

      ebook_data,

      categoryIds,
      level,
      getCategoryName
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
