require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

context 'A media-entry resource with get_metadata_and_previews permission' do
  before :each do
    @media_entry = FactoryGirl.create :media_entry,
                                      get_metadata_and_previews: true
  end

  context 'a meta datum of type text' do
    before :each do
      @meta_datum_text = FactoryGirl.create :meta_datum_text,
                                            media_entry: @media_entry
    end

    describe 'preconditions' do
      it 'exists' do
        expect(MetaDatum.find @meta_datum_text.id).to be
      end

      it 'belongs to the media-entry' do
        expect(@media_entry.meta_data).to include @meta_datum_text
      end
    end

    describe 'resource' do
      include_context :media_entry_resource_via_json_roa
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
    end
  end
end
