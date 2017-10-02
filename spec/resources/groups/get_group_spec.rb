require 'spec_helper'

context 'groups' do

  before :each do
    @group = FactoryGirl.create :group
  end

  context 'non admin user' do
    include_context :json_roa_client_for_authenticated_user do
      it 'is forbidden to retrieve any group' do
        expect(
          client.get.relation('group').get(id: @group.id).response.status
        ).to be==403
      end
    end
  end

  context 'admin user' do
    include_context :json_roa_client_for_authenticated_admin_user do

      context 'retriving a standard group' do
        let :get_group_result do
          client.get.relation('group').get(id: @group.id)
        end

        it 'works' do
          expect(get_group_result.response.status).to be==200
        end

        it 'lets us navigate to the group itself via the self-relation' do
          expect(get_group_result.json_roa_data['self-relation']['href']).to match /#{@group.id}/
        end

        it 'has the proper data, sans :searchable and :previous_id' do
          expect(get_group_result.data).to be== \
            @group.attributes.with_indifferent_access.except(:searchable, :previous_id)
        end
      end

      context 'a institunal group (with naughty institutional_id)' do
        before :each do
          @inst_group = FactoryGirl.create :institutional_group,
            institutional_id: '?this#id/needs/to/be/url&encoded'
        end
        it 'can be retrieved by the institutional_id' do
          expect(
            client.get.relation('group').get(id: @inst_group.institutional_id).response.status
          ).to be== 200
          expect(
            client.get.relation('group').get(id: @inst_group.institutional_id).data["id"]
          ).to be== @inst_group["id"]
        end
      end

    end
  end
end


