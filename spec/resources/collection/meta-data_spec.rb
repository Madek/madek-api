require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

context 'A collection resource with get_metadata_and_previews permission' do
  before :each do
    @collection = FactoryGirl.create :collection,
      get_metadata_and_previews: true
  end

  context 'a meta datum of type text' do
    before :each do
      @meta_datum_text = FactoryGirl.create :meta_datum_text,
        collection: @collection
    end

    describe 'preconditions' do
      it 'exists' do
        expect(MetaDatum.find @meta_datum_text.id).to be
      end

      it 'belongs to the collection' do
        expect(@collection.meta_data).to include @meta_datum_text
      end
    end

    describe 'resource' do
      include_context :collection_resource_via_json_roa
      it 'has a meta-data relation' do
        expect(resource.relation('meta-data')).to \
          be_a JSON_ROA::Client::Relation
      end

      describe 'get meta-data relation' do
        let :get_meta_data_relation do
          resource.relation('meta-data').get
        end

        it 'is a resource' do
          expect(get_meta_data_relation).to be_a JSON_ROA::Client::Resource
        end

        describe 'meta_data the resource' do
          let :meta_data_resource do
            get_meta_data_relation
          end

          describe 'the response' do
            it 'the status code indicates success' do
              expect(meta_data_resource.response.status).to be == 200
            end
          end
        end
      end

      describe 'get meta-data relation with query parameters' do
        describe 'set meta_keys to some string' do
          let :get_meta_data_relation do
            resource.relation('meta-data').get("meta_keys" => "bogus")
          end
          describe 'the response' do
            it '422s' do
              expect(get_meta_data_relation.response.status).to be == 422
            end
          end
        end
        describe 'set meta_keys to an json encoded array including the used key' do
          let :get_meta_data_relation do
            resource.relation('meta-data') \
              .get("meta_keys" => [@meta_datum_text.meta_key_id].to_json)
          end
          describe 'the response' do
            it 'succeeds' do
              expect(get_meta_data_relation.response.status).to be == 200
            end
            it 'contains the meta-datum ' do
              expect(get_meta_data_relation.data['meta-data'].map{|x| x[:id]}).to \
                include @meta_datum_text.id
            end
          end
        end

        describe 'set meta_keys to an json encoded array excluding the used key' do
          let :get_meta_data_relation do
            resource.relation('meta-data') \
              .get("meta_keys" => ['bogus'].to_json)
          end
          describe 'the response' do
            it 'succeeds' do
              expect(get_meta_data_relation.response.status).to be == 200
            end
            it 'does not contain the meta-datum ' do
              expect(get_meta_data_relation.data['meta-data'].map{|x| x[:id]}).not_to \
                include @meta_datum_text.id
            end
          end
        end


      end
    end
  end

  context 'A collection resource without get_metadata_and_previews permission' do
    before :each do
      @collection = FactoryGirl.create :collection,
        get_metadata_and_previews: false
    end

    context 'a meta datum of type text' do
      before :each do
        @meta_datum_text = FactoryGirl.create :meta_datum_text,
          collection: @collection
      end

      describe 'preconditions' do
        it 'exists' do
          expect(MetaDatum.find @meta_datum_text.id).to be
        end

        it 'belongs to the collection' do
          expect(@collection.meta_data).to include @meta_datum_text
        end
      end

      describe 'resource' do
        include_context :collection_resource_via_json_roa
        it '401s' do
          expect(response.status).to be== 401
        end

      end

    end
  end
end

