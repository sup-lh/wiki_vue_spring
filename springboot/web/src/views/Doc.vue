<template>
  <a-layout>
    <a-layout-content>
      <br>
      <h1 v-if="level.length === 0">《温馨提示》</h1>
      <br>
      <hr>
      <h2 v-if="level.length === 0">对不起，找不到该电子书的相关文档！</h2>
      <a-row>
        <a-col :span="6">
          <a-tree
              v-if="level.length > 0"
              :tree-data="level"
              @select="onSelect"
              :replaceFields="{title: 'name', key: 'id', value: 'id'}"
              :defaultExpandAll="true"
              :defaultSelectedKeys="defaultSelectedKeys"
          >
          </a-tree>
        </a-col>
        <a-col :span="18">
          <div>
            <h2>{{doc.name}}</h2>
            <div>
              <span>阅读数：{{doc.viewCount}}</span> &nbsp; &nbsp;
              <span>点赞数：{{doc.voteCount}}</span>
            </div>
            <a-divider style="height: 2px; background-color: #9999cc"/>
          </div>
          <div class="wangeditor" :innerHTML="html"></div>

          <div class="vote-div">
            <a-button type="primary" shape="round" :size="'large'" @click="vote">
              <template #icon>
                <LikeOutlined/> &nbsp;点赞
              </template>
            </a-button>
          </div>

        </a-col>
      </a-row>
    </a-layout-content>
  </a-layout>
</template>


<script lang="ts">
import {createVNode, defineComponent, onMounted, ref} from 'vue';
import axios from 'axios';
import {message, Modal} from "ant-design-vue";
import {Tool} from "@/util/tool";
import {useRoute} from "vue-router";

export default defineComponent({
  name: 'docVue',
  setup() {

    const route = useRoute()
    const level = ref()
    level.value = [];
    const html = ref()

    const defaultSelectedKeys = ref();
    defaultSelectedKeys.value = [];

    // 当前选中的文档
    const doc = ref();
    doc.value = {};


    const handleQueryContent = (id: any) => {
      axios.get("/doc/findContent/" + id).then((response) => {
        const data = response.data;
        if (data.success) {
          html.value = data.content
        }
      });
    };


    const handleQuery = () => {
      level.value = []
      axios.get("/doc/allDoc/" + route.query.ebookId).then((response) => {
        const data = response.data;
        if (data.success) {
          level.value = Tool.array3Tree(data.content, 0)

          if (Tool.isNotEmpty(level)) {
            defaultSelectedKeys.value = [level.value[0].id];
            handleQueryContent(level.value[0].id);
            // 初始显示文档信息
            doc.value = level.value[0];
          }

        } else {
          message.error(data.message);
        }
      });
    };


    const onSelect = (selectKey: any, info: any) => {
      if (Tool.isNotEmpty(selectKey)) {
        handleQueryContent(selectKey[0])
        // 选中某一节点时，加载该节点的文档信息
        doc.value = info.selectedNodes[0].props;
      }
    }

    // 点赞
    const vote = () => {
      axios.get('/doc/vote/' + doc.value.id).then((response) => {
        const data = response.data;
        if (data.success) {
          doc.value.voteCount++;
          console.log(doc.value.voteCount)
        } else {
          message.error(data.message);
        }
      });
    };


    onMounted(() => {
      handleQuery();

    });


    return {

      level,
      html,
      onSelect,

      defaultSelectedKeys,
      doc,

      vote
    }
  }
});
</script>

<style>
/* 点赞 */
.vote-div {
  padding: 15px;
  text-align: center;
}

/* 图片自适应 */
.wangeditor img {
  max-width: 100%;
  height: auto;
}
</style>