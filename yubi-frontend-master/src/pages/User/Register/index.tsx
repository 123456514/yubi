import Footer from '@/components/Footer';
import { userRegisterUsingPOST } from '@/services/yubi/userController';
import { Helmet, Link } from '@@/exports';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { message, Tabs } from 'antd';
import React from 'react';
import { useNavigate } from 'umi';
import Settings from '../../../../config/defaultSettings';

const UserRegister: React.FC = () => {
  const containerClassName = useEmotionCss(() => {
    return {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    };
  });
  const navigate = useNavigate();

  /**
   * 用户注册
   * @param fields
   */
  const doUserRegister = async (fields: API.UserRegisterRequest) => {
    if (fields.userPassword !== fields.checkPassword) {
      message.error('两次输入的密码不一致');
      return;
    }
    const hide = message.loading('注册中');
    try {
      await userRegisterUsingPOST({ ...fields });
      hide();
      message.success('注册成功');
      navigate('/user/login', {
        replace: true,
      });
    } catch (e) {
      hide();
      message.error('注册失败，请重试！');
    }
  };

  return (
    <div className={containerClassName}>
      <Helmet>
        <title>
          {'注册'}- {Settings.title}
        </title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={<img alt="logo" src="/logo.svg" />}
          title="智能 BI"
          subTitle={
            <a>
              致力于“取代”初级数据分析师
            </a>
          }
          submitter={{
            searchConfig: {
              submitText: '注册',
            },
          }}
          onFinish={async (values) => {
            await doUserRegister(values as API.UserRegisterRequest);
          }}
        >
          <Tabs
            centered
            items={[
              {
                key: 'register',
                label: '账号密码注册',
              },
            ]}
          />
          <>
            <ProFormText
              name="userAccount"
              fieldProps={{
                size: 'large',
                prefix: <UserOutlined />,
              }}
              placeholder={'请输入账号'}
              rules={[
                {
                  required: true,
                  message: '请输入账号！',
                },
              ]}
            />
            <ProFormText.Password
              name="userPassword"
              fieldProps={{
                size: 'large',
                prefix: <LockOutlined />,
              }}
              placeholder={'请输入密码'}
              rules={[
                {
                  required: true,
                  message: '请输入密码！',
                },
              ]}
            />
            <ProFormText.Password
              name="checkPassword"
              fieldProps={{
                size: 'large',
                prefix: <LockOutlined />,
              }}
              placeholder={'请输入确认密码'}
              rules={[
                {
                  required: true,
                  message: '请输入确认密码！',
                },
              ]}
            />
          </>
          <div
            style={{
              marginBottom: 24,
            }}
          >
            <Link to="/user/login">老用户登录</Link>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};
export default UserRegister;
