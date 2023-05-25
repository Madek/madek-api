require 'spec_helper'

describe 'index' do

  # clear some tables filled by the seeds
  before :each do
    ActiveRecord::Base.connection.execute <<-SQL
      TRUNCATE TABLE meta_keys CASCADE;
    SQL
  end

  include_context :json_roa_client_for_authenticated_user do
    let :meta_keys_resource do
      json_roa_client.get.relation('meta-keys').get
    end

    it 'should return 200 with only viewable by public meta-keys' do
      vocab = FactoryBot.create(:vocabulary, enabled_for_public_view: false)
      meta_key = FactoryBot.create(:meta_key,
                                    id: "#{vocab.id}:#{Faker::Lorem.word}",
                                    vocabulary: vocab)
      expect(meta_keys_resource.response.status).to be == 200
      expect(meta_keys_resource.data['meta-keys'].count).to be == 0
    end

    context 'when user is authenticated' do
      context 'when view permission is true' do
        it 'returns meta key in collection through the user permissions' do
          vocabulary = FactoryBot.create(:vocabulary,
                                          enabled_for_public_view: false)
          meta_key = FactoryBot.create(:meta_key,
                                        id: "#{vocabulary.id}:#{Faker::Lorem.word}",
                                        vocabulary: vocabulary)
          Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                        view: true,
                                                        vocabulary: vocabulary)

          data = client.get.relation('meta-keys').get.data['meta-keys'].first

          expect(data).to have_key 'id'
          expect(data['id']).to eq meta_key.id
        end

        it 'returns meta key in collection through the group permissions' do
          vocabulary = FactoryBot.create(:vocabulary,
                                          enabled_for_public_view: false)
          meta_key = FactoryBot.create(:meta_key,
                                        id: "#{vocabulary.id}:#{Faker::Lorem.word}",
                                        vocabulary: vocabulary)
          group = FactoryBot.create :group
          group.users << user
          Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                         view: true,
                                                         vocabulary: vocabulary)

          data = client.get.relation('meta-keys').get.data['meta-keys'].first

          expect(data).to have_key 'id'
          expect(data['id']).to eq meta_key.id
        end
      end

      context 'when view permission is false' do
        it 'does not return meta key through the user permissions' do
          vocabulary = FactoryBot.create(:vocabulary,
                                          enabled_for_public_view: false)
          meta_key = FactoryBot.create(:meta_key,
                                        id: "#{vocabulary.id}:#{Faker::Lorem.word}",
                                        vocabulary: vocabulary)
          Permissions::VocabularyUserPermission.create!(user_id: user.id,
                                                        view: false,
                                                        vocabulary: vocabulary)

          data = client.get.relation('meta-keys').get.data['meta-keys']

          expect(data.count).to be_zero
        end

        it 'does not return meta key through the group permissions' do
          vocabulary = FactoryBot.create(:vocabulary,
                                          enabled_for_public_view: false)
          meta_key = FactoryBot.create(:meta_key,
                                        id: "#{vocabulary.id}:#{Faker::Lorem.word}",
                                        vocabulary: vocabulary)
          group = FactoryBot.create :group
          group.users << user
          Permissions::VocabularyGroupPermission.create!(group_id: group.id,
                                                                      view: false,
                                                                      vocabulary: vocabulary)

          data = client.get.relation('meta-keys').get.data['meta-keys']

          expect(data.count).to be_zero
        end
      end
    end
  end
end
