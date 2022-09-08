<template>
  <a-layout>
    <a-layout-sider width="200" style="background: #fff">


      <a-menu mode="inline" :style="{ height: '100%', borderRight: 0 }" @click="handleClick">

        <a-menu-item key="welcome">
          <MailOutlined/>
          <span>欢迎</span>
        </a-menu-item>

        <a-sub-menu v-for="item in level" :key="item.id" :title="item.name">
          <a-menu-item :key="item.sort">
            <pre>    (ALL)</pre>
          </a-menu-item>
          <a-menu-item v-for="child in item.children" :key="child.sort" :title="child.name">
            <MailOutlined/>
            <span>{{child.name}}</span>
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="tip" :disabled="true">
          <span>以上菜单在分类管理配置</span>
        </a-menu-item>

      </a-menu>

    </a-layout-sider>
    <a-layout-content :style="{ background: '#fff', padding: '44px', margin: 0, minHeight: '280px' }">

      <div class="welcome" v-show="isShowWelcome">
        <the-welcome></the-welcome>
      </div>

      <a-list v-show="!isShowWelcome" item-layout="vertical" size="large" :grid="{ gutter: 20,column:3}"
              :data-source="ebooks" :style="{ left:'2px',margin: 0 }">
        <template #renderItem="{ item }">
          <a-list-item key="item.bookName">
            <template #actions>
              <span>
                <component v-bind:is="'FileOutlined'" style="margin-right: 8px"/>
                {{ item.docNum }}
              </span>
              <span>
                <component v-bind:is="'UserOutlined'" style="margin-right: 8px"/>
                {{ item.viewNum }}
              </span>
              <span>
                <component v-bind:is="'LikeOutlined'" style="margin-right: 8px"/>
                {{ item.voteNum }}
              </span>
            </template>
            <a-list-item-meta :description="item.desCription">
              <template #title>
                <router-link :to="'/doc?ebookId=' + item.id">
                  {{ item.bookName }}
                </router-link>
              </template>
              <template #avatar>
                <a-avatar :src="item.cover"/>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
import {defineComponent, onMounted, ref, reactive} from 'vue';
import axios from "axios";
import {Tool} from "@/util/tool";
import {message} from "ant-design-vue";
import TheWelcome from '@/components/the-welcome.vue';


const listData: Record<string, string>[] = [];


export default defineComponent({
  name: 'Home',
  components: {
    TheWelcome
  },
  setup() {
    console.log('setup')
    const content = ref()
    const ebooks = ref()


    const level = ref();
    let categorys: any;
    const handleQueryCategory = () => {
      axios.get("/category/allCategory").then((response) => {
        const data = response.data;
        if (data.success) {
          categorys = data.content;
          handleQueryEbook();
          level.value = Tool.array2Tree(data.content, 0)  // array2Tree处理显示树形结构的问题
        } else {
          message.error(data.message);
        }
      });
    };

    let categoryId2 = 0;
    const handleQueryEbook = () => {
      axios.get("/ebook/list", {
        params: {
          page: 1,
          size: 100,
          category2Id: categoryId2
        }
      }).then((response) => {
        const data = response.data;
        ebooks.value = data.content.list;
      });
    };

    let sort = 0
    const handleQueryEbookSort = () => {
      axios.get("/ebook/list", {
        params: {
          page: 1,
          size: 100,
          category1Id: sort
        }
      }).then((response) => {
        const data = response.data;
        ebooks.value = data.content.list;
      });
    };


    const isShowWelcome = ref(true);
    const handleClick = (value: any) => {
      console.log(value)
      if (value.key === 'welcome') {
        isShowWelcome.value = true;
      } else {
        if (value.key % 100 == 0) {
          sort = value.key
          handleQueryEbookSort()
        } else {
          categoryId2 = value.key;
          isShowWelcome.value = false;
          handleQueryEbook();
        }
      }
    };


    onMounted(() => {
      handleQueryCategory()
    })

    return {
      content,
      listData,
      pagination: {
        onChange: (page: number) => {
          console.log(page);
        },
        pageSize: 10000000000000,
      },

      level,

      isShowWelcome,
      handleClick,
      ebooks,


    }

  }
});
</script>


<style scoped>
.ant-avatar {
  width: 50px;
  height: 50px;
  line-height: 50px;
  border-radius: 8%;
  margin: 5px 0;
}

</style>
