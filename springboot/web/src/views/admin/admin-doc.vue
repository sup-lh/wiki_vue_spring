<template>

  <a-row>
    <a-col :span="1">
    </a-col>
    <a-col :span="8">
      <a-layout>
        <p>
          <a-form>
            <a-form-model layout="inline" :model="param">
              <br>

              <a-form-model-item>
                <a-button
                    type="primary"
                    @click="handleQuery"
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
              v-if="level.length > 0"
              :columns="columns"
              :row-key="record => record.id"
              :data-source="level"
              :pagination="false"
              :loading="loading"
              size="small"
              :defaultExpandAllRows="true"
          >
            <template #cover="{ text, record }">
              {{ record.sort }} . {{text}}
            </template>

            <template v-slot:fatherId="{ text, record }">
              <span>{{ getParentName(record.parent)  }}</span>
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
                    @confirm="del(record.fatherId)"
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
    </a-col>
    <a-col :span="1">
    </a-col>
    <a-col :span="13">
      <p>
        <a-form>
          <a-form-item>
            <a-button type="primary" @click="handleOk">
              保存
            </a-button>
          </a-form-item>
        </a-form>
      </p>
      <a-form :model="doc_data" layout="vertical" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-form-item>
          <a-input v-model:value="doc_data.name" placeholder="文档名"/>
        </a-form-item>
        <a-form-item>
          <a-tree-select
              show-search
              style="width: 100%"
              :dropdown-style="{ maxHeight: '400px', }"
              :tree-data="treeSelectData"
              placeholder="请选择父级文档"
              allow-clear
              tree-default-expand-all
              :replaceFields="{  title:'name', key:'fatherId', value:'fatherId'}"
              v-model:value="doc_data.parent"
          >
          </a-tree-select>
        </a-form-item>

        <a-form-item>
          <a-input v-model:value="doc_data.sort" placeholder="顺序"/>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="handlePreviewContent()">
            <EyeOutlined/>
            内容预览
          </a-button>
        </a-form-item>

        <a-form-item>
          <div id="content"></div>
        </a-form-item>
      </a-form>
    </a-col>
    <a-col :span="1">
    </a-col>
  </a-row>

  <a-drawer width="900" placement="right" :closable="false" :visible="drawerVisible" @close="onDrawerClose">
    <div class="wangeditor" :innerHTML="previewHtml"></div>
  </a-drawer>

</template>

<script lang="ts">
import {createVNode, defineComponent, onMounted, ref} from 'vue';
import axios from 'axios';
import {message, Modal} from "ant-design-vue";
import {Tool} from "@/util/tool";
import {useRoute} from "vue-router";
import {ExclamationCircleOutlined} from '@ant-design/icons-vue';
import E from 'wangeditor'

export default defineComponent({
  name: 'Doc',
  setup() {

    const route = useRoute()

    const param = ref();
    param.value = {};

    const loading = ref(false);

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        slots: {customRender: 'cover'}

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
    level.value = [];
    // 在 编辑框 中调用的level中需要知道：
    //  :value 是返回值的设置，应该设置为sort，就是将拿到的这个树形结构中的这个选择的父分类的sort返回回去当作这个编辑内容的parent


    let docs: any;

    // treeSelectData 里面有{fatherId：0，name：无}
    const treeSelectData = ref();
    treeSelectData.value = [];


    const handleQuery = () => {
      level.value = []
      loading.value = true;
      axios.get("/doc/allDoc/" + route.query.ebookId).then((response) => {
        loading.value = false;
        const data = response.data;
        docs = data.content;
        if (data.success) {
          level.value = Tool.array3Tree(data.content, 0)

          // 增加数据时，不需要进行disable操作
          treeSelectData.value = Tool.copy(level.value) || [];
          treeSelectData.value.unshift({fatherId: 0, name: '无'});

          console.log('请求成功了，level的值为：', level.value)
        } else {
          message.error(data.message);
          console.log('没请求成功，level的值为：', level.value)
        }
      });
    };


    // 表单
    const doc_data = ref();
    doc_data.value = []
    const modalText = ref<string>('Content of the modal');
    const confirmLoading = ref<boolean>(false);
    const editor = new E('#content')
    editor.config.zIndex = 0


    // 点击ok键时的处理
    const handleOk = () => {
      confirmLoading.value = true;
      doc_data.value.content = editor.txt.html();
      axios.post("/doc/saveDoc", doc_data.value).then((response) => {
        confirmLoading.value = false;
        if (response.data.success) {
          message.success("保存成功")
          handleQuery();
        } else {
          message.error(response.data.message)
        }
      });
    };

    /**
     * 将某节点及其子孙节点全部置为disabled
     */
    const setDisable = (fatherId: any, treeSelectData: any, flag: any) => {
      // console.log(treeSelectData, id);
      // 遍历数组，即遍历某一层节点
      for (let i = 0; i < treeSelectData.length; i++) {
        const node = treeSelectData[i];

        let node1: any
        if (flag == 'true') {
          node1 = node.fatherId
        } else {
          node1 = node.parent
        }

        if (fatherId === node1) {
          // 将目标节点设置为disabled
          node.disabled = true;
          // 如果当前节点就是目标节点
          console.log(node.name, node)
          // 遍历所有子节点，将所有子节点全部都加上disabled
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            for (let j = 0; j < children.length; j++) {
              setDisable(children[j].parent, children, 'false')
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看。
          if (Tool.isNotEmpty(node.children)) {
            setDisable(fatherId, node.children, 'true');
          }
        }
      }
    };

    let ids: any = []
    let deleteNames: any = [];

    const getDeleteIds = (treeSelectData: any, fatherId: any) => {
      // console.log(treeSelectData, id);
      // 遍历数组，即遍历某一层节点
      for (let i = 0; i < treeSelectData.length; i++) {
        const node = treeSelectData[i];
        if (node.fatherId === fatherId) {
          // 如果当前节点就是目标节点
          console.log("delete", node);
          // 将目标ID放入结果集ids
          ids.push(fatherId);
          deleteNames.push(node.name);

          // 遍历所有子节点
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            for (let j = 0; j < children.length; j++) {
              getDeleteIds(children, children[j].fatherId)
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看。
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            getDeleteIds(children, fatherId);
          }
        }
      }
    };


    // 修改数据
    const edit = (record: any) => {
      editor.txt.html("")
      doc_data.value = Tool.copy(record)
      handleQueryContent();

      // 修改数据时，需要进行disable操作
      // 将子孙节点添加disable,不可选择
      treeSelectData.value = Tool.copy(level.value);
      setDisable(record.fatherId, treeSelectData.value, 'true');
      // 向数组的前面添加一个元素
      treeSelectData.value.unshift({fatherId: 0, name: '无'});


    }
    // 新增
    const add = () => {
      editor.txt.html("")

      doc_data.value = {
        ebookId: route.query.ebookId
      }

      // 增加数据时，不需要进行disable操作
      treeSelectData.value = Tool.copy(level.value) || [];
      treeSelectData.value.unshift({fatherId: 0, name: '无'});

    }
    // 删除
    const del = (fatherId: number) => {
      // 拿到整棵树的fatherId
      getDeleteIds(level.value, fatherId)
      console.log(deleteNames)
      Modal.confirm({
        title: '重要提醒',
        icon: createVNode(ExclamationCircleOutlined),
        content: '将删除：【' + deleteNames.join("，") + "】删除后不可恢复，确认删除？",
        onOk() {
          axios.delete("/doc/deleteDoc/" + ids.join(',')).then((response) => {
            if (response.data.success) {
              handleQuery();
            }
            ids = []
            deleteNames = []
          });
        },
      });
    }

    const getParentName = (parent: number) => {
      let result = "";
      docs.forEach((item: any) => {
        if (Number(item.fatherId) === Number(parent)) {
          // return item.name; // 注意，这里直接return不起作用
          result = item.name;
        }
      });
      return result;
    };


    const handleQueryContent = () => {
      axios.get("/doc/findContent/" + doc_data.value.id).then((response) => {
        const data = response.data;
        if (data.success) {
          editor.txt.html(data.content)
        }
      });
    };

    // ----------------富文本预览--------------
    const drawerVisible = ref(false);
    const previewHtml = ref();
    const handlePreviewContent = () => {
      const html = editor.txt.html();
      previewHtml.value = html;
      drawerVisible.value = true;
    };
    const onDrawerClose = () => {
      drawerVisible.value = false;
    };


    onMounted(() => {
      // 初始渲染，页数：1  ，条数：pagination的静态数据，3
      handleQuery();
      editor.create();
      add();

    });


    return {
      param,
      columns,
      loading,
      handleQuery,

      // 表单
      modalText,
      confirmLoading,
      handleOk,

      edit,
      add,
      del,

      doc_data,
      level,

      getParentName,

      treeSelectData,

      drawerVisible,
      previewHtml,
      handlePreviewContent,
      onDrawerClose,

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
